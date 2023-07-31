package ru.netology.laliapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.laliapp.entity.EventRemoteKeyEntity

@Dao
interface EventRemoteKeyDao {

    @Query("SELECT COUNT(*) == 0 FROM EventRemoteKeyEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT MAX(`key`) FROM EventRemoteKeyEntity")
    suspend fun max(): Long?

    @Query("SELECT MIN(`key`) FROM EventRemoteKeyEntity")
    suspend fun min(): Long?

    @Query("DELETE FROM EventRemoteKeyEntity")
    suspend fun removeAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(eventRemoteKeyEntity: EventRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(eventRemoteKeyEntity: List<EventRemoteKeyEntity>)
}