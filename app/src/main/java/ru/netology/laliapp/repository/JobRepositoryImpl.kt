package ru.netology.laliapp.repository

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.laliapp.api.JobApiService
import ru.netology.laliapp.dao.JobDao
import ru.netology.laliapp.dto.Job
import ru.netology.laliapp.entity.JobEntity
import ru.netology.laliapp.entity.toDto
import ru.netology.laliapp.entity.toJobEntity
import ru.netology.laliapp.errors.ApiError
import ru.netology.laliapp.errors.NetworkError
import java.io.IOException
import javax.inject.Inject


class JobRepositoryImpl @Inject constructor(

    private val jobDao: JobDao,
    private val jobApiService: JobApiService
) : JobRepository {

    override val data: Flow<List<Job>> = jobDao.getJob()
        .map { it.toDto() }
        .flowOn(Dispatchers.Default)

    private val _data = MutableLiveData<List<Job>>()

    override suspend fun saveJob(job: Job) {
        try {
            val response = jobApiService.saveJob(job)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insertJob(JobEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError()
        }
    }

    override suspend fun getJobByUserId(id: Long) {
        try {
            jobDao.deleteAll()
            val response = jobApiService.getJobByUserId(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            _data.postValue(body)
            jobDao.insertJobs(body.toJobEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError()
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            val response = jobApiService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            jobDao.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError()
        }
    }
}