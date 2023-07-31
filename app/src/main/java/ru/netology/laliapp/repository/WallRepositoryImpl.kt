package ru.netology.laliapp.repository

import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.netology.laliapp.api.WallApiService
import ru.netology.laliapp.dao.*
import ru.netology.laliapp.db.AppDb
import ru.netology.laliapp.dto.Post
import ru.netology.laliapp.entity.PostEntity
import javax.inject.Inject

class WallRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val wallApiService: WallApiService,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
) : WallRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun loadUserWall(userId: Long): Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        remoteMediator = WallRemoteMediator(
            wallApiService = wallApiService,
            postDao = postDao,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb,
            authorId = userId,
        ),
        pagingSourceFactory = { postDao.getPagingSource(userId) }
    ).flow
        .map {
            it.map(PostEntity::toDto)
        }
}