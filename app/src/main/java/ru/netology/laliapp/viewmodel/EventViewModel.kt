package ru.netology.laliapp.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.laliapp.auth.AppAuth
import ru.netology.laliapp.dto.Coordinates
import ru.netology.laliapp.dto.Event
import ru.netology.laliapp.dto.MediaUpload
import ru.netology.laliapp.enumeration.AttachmentType
import ru.netology.laliapp.enumeration.EventType
import ru.netology.laliapp.model.MediaModel
import ru.netology.laliapp.model.StateModel
import ru.netology.laliapp.repository.EventRepository
import ru.netology.laliapp.utils.SingleLiveEvent
import java.io.InputStream
import java.time.Instant
import javax.inject.Inject

private val emptyEvent = Event(
    id = 0,
    authorId = 0,
    author = "",
    authorAvatar = "",
    content = "",
    published = Instant.now().toString(),
    datetime = Instant.now().toString(),
    type = EventType.ONLINE,
    speakerIds = emptySet()
)

private val noMedia = MediaModel()

@ExperimentalCoroutinesApi
@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    appAuth: AppAuth,
) : ViewModel() {

    private val cached = eventRepository
        .data
        .cachedIn(viewModelScope)

    val data: Flow<PagingData<Event>> =
        appAuth.authStateFlow
            .flatMapLatest { (myId, _) ->
                cached.map { pagingData ->
                    pagingData.map { event ->
                        event.copy(
                            ownedByMe = event.authorId == myId,
                            likedByMe = event.likeOwnerIds.contains(myId),
                            participatedByMe = event.participantsIds.contains(myId)
                        )
                    }
                }
            }

    private val _edited = MutableLiveData(emptyEvent)
    val edited: LiveData<Event>
        get() = _edited

    private val _dataState = MutableLiveData<StateModel>()
    val dataState: LiveData<StateModel>
        get() = _dataState

    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated

    private val _media = MutableLiveData(noMedia)
    val media: LiveData<MediaModel>
        get() = _media

    private val scope = MainScope()

    fun save() {
        _edited.value?.let { event ->
            viewModelScope.launch {
                _dataState.postValue(StateModel(loading = true))
                try {
                    when (_media.value) {
                        noMedia ->
                            eventRepository.saveEvent(event)
                        else ->
                            _media.value?.inputStream?.let {
                                MediaUpload(it)
                            }?.let {
                                eventRepository.saveWithAttachment(event, it)
                            }
                    }
                    _dataState.value = StateModel()
                    _eventCreated.value = Unit
                } catch (e: Exception) {
                    throw UnknownError()
                }
            }
        }
        _edited.value = emptyEvent
        _media.value = noMedia
    }

    fun changeContent(content: String, date: String, coords: Coordinates?, type: EventType, link: String?) {


        _edited.value?.let {

            if (it.datetime != date ||
                it.type != type ||
                it.link != link?.trim() ||
                it.coords != coords ||
                it.content != content.trim()
            )

                _edited.value = it.copy(
                    datetime = date,
                    type = type,
                    link = link?.trim(),
                    coords = coords,
                    content = content.trim()
                )
        }

    }

    fun setSpeaker(id: Long) {
        if (edited.value?.speakerIds?.contains(id) == false) {
            _edited.value = edited.value?.speakerIds?.plus(id)?.let {
                edited.value?.copy(speakerIds = it)
            }
        }
    }

    fun changeMedia(
        uri: Uri?,
        inputStream: InputStream?,
        type: AttachmentType?,
    ) {
        _media.value = MediaModel(uri, inputStream, type)
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            eventRepository.removeById(id)
        } catch (e: Exception) {
            _dataState.value = StateModel(error = true)
        }
    }

    fun edit(event: Event) {
        _edited.value = event
    }

    fun likeById(id: Long) = viewModelScope.launch {
        try {
            eventRepository.likeById(id)
        } catch (e: Exception) {
            _dataState.value = StateModel(error = true)
        }
    }

    fun unlikeById(id: Long) = viewModelScope.launch {
        try {
            eventRepository.unlikeById(id)
        } catch (e: Exception) {
            _dataState.value = StateModel(error = true)
        }
    }

    fun participate(id: Long) = viewModelScope.launch {
        try {
            eventRepository.participate(id)
        } catch (e: Exception) {
            _dataState.value = StateModel(error = true)
        }
    }

    fun doNotParticipate(id: Long) = viewModelScope.launch {
        try {
            eventRepository.doNotParticipate(id)
        } catch (e: Exception) {
            _dataState.value = StateModel(error = true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}