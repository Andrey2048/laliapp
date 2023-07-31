package ru.netology.laliapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.laliapp.auth.AppAuth
import ru.netology.laliapp.dto.Job
import ru.netology.laliapp.model.JobModel
import ru.netology.laliapp.model.StateModel
import ru.netology.laliapp.repository.JobRepository
import ru.netology.laliapp.utils.SingleLiveEvent
import javax.inject.Inject

private val empty = Job(
    id = 0,
    name = "",
    position = "",
    start = "",
    finish = null,
)

@ExperimentalCoroutinesApi
@HiltViewModel
class JobViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    appAuth: AppAuth,
) : ViewModel() {

    val data: Flow<List<Job>> =
        appAuth.authStateFlow
            .flatMapLatest { (myId, _) ->
                jobRepository.data.map {
                    JobModel()
                    it.map { job ->
                        job.copy(
                            ownedByMe = userId.value == myId
                        )
                    }
                }
            }
    private val userId = MutableLiveData<Long>()

    private val _edited = MutableLiveData(empty)

    private val _dataState = MutableLiveData<StateModel>()
    val dataState: LiveData<StateModel>
        get() = _dataState

    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated

    fun loadJobs(id: Long) = viewModelScope.launch {
        _dataState.postValue(StateModel(loading = true))
        try {
            jobRepository.getJobByUserId(id)
            _dataState.value = StateModel()
        } catch (e: Exception) {
            _dataState.postValue(StateModel(error = true))
        }
    }

    fun setId(id: Long) {
        userId.value = id
    }

    fun save() {
        _edited.value?.let { job ->
            viewModelScope.launch {
                _dataState.postValue(StateModel(loading = true))
                try {
                    jobRepository.saveJob(job)
                    _dataState.postValue(StateModel())
                    _jobCreated.value = Unit
                } catch (e: Exception) {
                    _dataState.postValue(StateModel(error = true))
                }
            }
        }
        _edited.value = empty
    }

    fun changeJobData(
        name: String,
        position: String,
        start: String,
        finish: String?,
        link: String?,
    ) {

        _edited.value?.let {
            if (it.name != name.trim() ||
                it.position != position.trim() ||
                it.start != start ||
                it.finish != finish ||
                it.link != link?.trim()
            )
                _edited.value = it.copy(
                    name = name.trim(),
                    position = position.trim(),
                    start = start,
                    finish = finish,
                    link = link?.trim()
                )
        }


    }

    fun edit(job: Job) {
        _edited.value = job
    }

    fun removeById(id: Long) =
        viewModelScope.launch {
            _dataState.postValue(StateModel(loading = true))
            try {
                jobRepository.removeById(id)
                _dataState.postValue(StateModel())
            } catch (e: Exception) {
                _dataState.postValue(StateModel(error = true))
            }
        }

    fun startDate(date: String) {
        _edited.value = _edited.value?.copy(start = date)
    }

    fun finishDate(date: String) {
        _edited.value = _edited.value?.copy(finish = date)
    }
}