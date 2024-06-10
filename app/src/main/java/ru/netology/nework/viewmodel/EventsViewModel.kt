package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Event
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.media.Media
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.repository.EventsRepository
import ru.netology.nework.repository.PostsRepository
import ru.netology.nework.viewmodel.AuthViewModel.Companion.myID
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi

class EventsViewModel @Inject constructor(
    private val repositoryEvents: EventsRepository,
    private val repositoryPosts: PostsRepository
) : ViewModel() {

//    val events: LiveData<List<Event>> = repositoryEvents.eventsDb
//        .asLiveData(Dispatchers.IO)

    val events = repositoryEvents.eventsDb
        .map {event ->
            event.map {
                it.copy(eventOwner = it.authorId == myID)
            }
        }
        .cachedIn(viewModelScope)
        .flowOn(Dispatchers.Default)

    val receivedEvents: LiveData<List<Event>>
        get() = repositoryEvents.eventsFlow.asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    init {
        getEvents()
    }

    private fun getEvents() {
        viewModelScope.launch {
            repositoryEvents.getEventsDB()
        }
    }

//    fun loadEvents() {
//        _dataState.value = FeedModelState(loading = true)
//        viewModelScope.launch {
//            try {
//                repositoryEvents.getEvents()
//                _dataState.value = FeedModelState()
//            } catch (e: Exception) {
//                if (e.javaClass.name == "ru.netology.nework.error.ApiError403") {
//                    _dataState.value = FeedModelState(error403 = true)
//                } else if (e.javaClass.name == "ru.netology.nework.error.NetworkError")
//                    _dataState.value = FeedModelState(errorNetWork = true)
//            }
//        }
//    }

    fun likeEvent(event: Event, like: Boolean) {
        _dataState.value = FeedModelState(loading = true)
        viewModelScope.launch {
            try {
                repositoryEvents.likeEvent(event.id!!, like)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                when (e.javaClass.name) {
                    "ru.netology.nework.error.ApiError403" -> {
                        _dataState.value = FeedModelState(error403 = true)
                    }

                    "ru.netology.nework.error.ApiError404" -> {
                        _dataState.value = FeedModelState(error404 = true)
                    }

                    else -> {
                        _dataState.value = FeedModelState(error = true)
                    }
                }
            }
        }
    }

    fun saveEvent(event: Event, media: MultipartBody.Part?, typeAttach: AttachmentType?) {
        _dataState.value = FeedModelState(loading = true)
        viewModelScope.launch {
            try {
                if (typeAttach != null && media != null) {
                    val _media: Media = repositoryPosts.upload(media)
                    val eventWithAttachment =
                        event.copy(attachment = Attachment(_media.url, typeAttach))
                    repositoryEvents.saveEvent(eventWithAttachment)
                    _dataState.value = FeedModelState()
                } else {
                    repositoryEvents.saveEvent(event)
                }
                _dataState.value = FeedModelState()

            } catch (e: Exception) {
                when (e.javaClass.name) {
                    "ru.netology.nework.error.ApiError403" -> {
                        _dataState.value = FeedModelState(error403 = true)
                    }

                    "ru.netology.nework.error.ApiError415" -> {
                        _dataState.value = FeedModelState(error415 = true)
                    }

                    else -> {
                        _dataState.value = FeedModelState(error = true)
                    }
                }
            }
        }
    }

    //deleteEvent
    fun removeEvent(event: Event) {
        _dataState.value = FeedModelState(loading = true)
        viewModelScope.launch {
            try {
                repositoryEvents.deleteEvent(event)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                when (e.javaClass.name) {
                    "ru.netology.nework.error.ApiError403" -> {
                        _dataState.value = FeedModelState(error403 = true)
                    }

                    "ru.netology.nework.error.ApiError404" -> {
                        _dataState.value = FeedModelState(error404 = true)
                    }

                    else -> {
                        _dataState.value = FeedModelState(error = true)
                    }
                }
            }
        }
    }

    fun participateEvent(event: Event, status: Boolean) {
        _dataState.value = FeedModelState(loading = true)
        viewModelScope.launch {
            try {
                repositoryEvents.participateEvent(event.id!!, status)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                when (e.javaClass.name) {
                    "ru.netology.nework.error.ApiError403" -> {
                        _dataState.value = FeedModelState(error403 = true)
                    }

                    "ru.netology.nework.error.ApiError404" -> {
                        _dataState.value = FeedModelState(error404 = true)
                    }

                    else -> {
                        _dataState.value = FeedModelState(error = true)
                    }
                }
            }
        }
    }

}