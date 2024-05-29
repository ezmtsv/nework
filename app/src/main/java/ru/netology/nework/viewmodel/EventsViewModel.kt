package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Event
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.media.Media
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.repository.EventsRepository
import ru.netology.nework.repository.PostsRepository
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi

class EventsViewModel @Inject constructor(
    private val repositoryEvents: EventsRepository,
    private val repositoryPosts: PostsRepository
) : ViewModel() {

    val events: LiveData<List<Event>> = repositoryEvents.eventsDb
        .asLiveData(Dispatchers.IO)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    fun loadEvents() {
        _dataState.value = FeedModelState(loading = true)
        viewModelScope.launch {
            try {
                repositoryEvents.getEvents()
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                if (e.javaClass.name == "ru.netology.nework.error.ApiError403") {
                    _dataState.value = FeedModelState(error403 = true)
                } else if (e.javaClass.name == "ru.netology.nework.error.NetworkError")
                    _dataState.value = FeedModelState(errorNetWork = true)
            }
        }
    }

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
                if (typeAttach != null) {
                    media?.let {
                        val _media: Media = repositoryPosts.upload(media)
                        val eventWithAttachment =
                            event.copy(attachment = Attachment(_media.url, typeAttach))
                        repositoryEvents.saveEvent(eventWithAttachment)
                        _dataState.value = FeedModelState()
                    }
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

}