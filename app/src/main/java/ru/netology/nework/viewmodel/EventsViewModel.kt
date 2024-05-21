package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Post
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.repository.EventsRepository
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi

class EventsViewModel @Inject constructor(
    private val repositoryEvents: EventsRepository
): ViewModel() {

    val events: LiveData<List<Event>> = repositoryEvents.eventsDb
        .asLiveData(Dispatchers.IO)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _location = MutableLiveData<Point>()
    val location: LiveData<Point>
        get() = _location

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

}