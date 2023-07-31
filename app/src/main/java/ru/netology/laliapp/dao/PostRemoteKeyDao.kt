package ru.netology.laliapp.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.REPLACE
import ru.netology.laliapp.entity.PostRemoteKeyEntity

@Dao
interface PostRemoteKeyDao {

    @Query("SELECT COUNT(*) == 0 FROM PostRemoteKeyEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT MAX(`key`) FROM PostRemoteKeyEntity")
    suspend fun max(): Long?

    @Query("SELECT MIN(`key`) FROM PostRemoteKeyEntity")
    suspend fun min(): Long?

    @Query("DELETE FROM PostRemoteKeyEntity")
    suspend fun removeAll()

    @Insert(onConflict = REPLACE)
    suspend fun insert(postRemoteKeyEntity: PostRemoteKeyEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insert(postRemoteKeyEntity: List<PostRemoteKeyEntity>)
}