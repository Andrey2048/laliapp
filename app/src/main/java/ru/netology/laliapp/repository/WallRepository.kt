package ru.netology.laliapp.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.laliapp.dto.Post

interface WallRepository {

    fun loadUserWall(userId: Long): Flow<PagingData<Post>>
}