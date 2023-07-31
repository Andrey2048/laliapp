package ru.netology.laliapp.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.LoadType.*
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.laliapp.api.WallApiService
import ru.netology.laliapp.dao.*
import ru.netology.laliapp.db.AppDb
import ru.netology.laliapp.entity.PostEntity
import ru.netology.laliapp.entity.PostRemoteKeyEntity
import ru.netology.laliapp.errors.ApiError

@OptIn(ExperimentalPagingApi::class)
class WallRemoteMediator(
    private val postDao: PostDao,
    private val wallApiService: WallApiService,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
    private val authorId: Long,
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>,
    ): MediatorResult {
        try {
            val result = when (loadType) {
                REFRESH ->
                    wallApiService.getWallLatest(authorId, state.config.pageSize)

                APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    wallApiService.getWallBefore(authorId, id, state.config.pageSize)
                }

                PREPEND -> {
                    return MediatorResult.Success(true)
                }
            }

            if (!result.isSuccessful) {
                throw ApiError(result.code(), result.message())
            }

            val body = result.body() ?: throw ApiError(result.code(), result.message())

            appDb.withTransaction {
                when (loadType) {
                    REFRESH -> {
                        postRemoteKeyDao.removeAll()
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                body.first().id
                            )
                        )

                        if (postRemoteKeyDao.isEmpty()) {
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id
                                )
                            )
                        }
                    }

                    APPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id,
                            ),
                        )
                    }

                    PREPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                body.first().id,
                            ),
                        )
                    }

                }
                postDao.insertPosts(body.map(PostEntity::fromDto))
            }
            return MediatorResult.Success(body.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}