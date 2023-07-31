package ru.netology.laliapp.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.laliapp.dto.User

interface UserRepository {

    val data: Flow<List<User>>

    suspend fun getAll()
}