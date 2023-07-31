package ru.netology.laliapp.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.laliapp.dto.Job

interface JobRepository {

    val data: Flow<List<Job>>

    suspend fun saveJob(job: Job)
    suspend fun getJobByUserId(id: Long)
    suspend fun removeById(id: Long)
}