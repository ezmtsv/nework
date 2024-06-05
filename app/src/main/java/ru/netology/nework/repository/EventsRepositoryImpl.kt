package ru.netology.nework.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dto.Event
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.toDto
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.ApiError403
import ru.netology.nework.error.ApiError404
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import ru.netology.nework.viewmodel.AuthViewModel
import java.io.IOException
import javax.inject.Inject

class EventsRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val daoEvents: EventDao,
) : EventsRepository {

    private val _eventsDb = daoEvents.getEvents().map(List<EventEntity>::toDto)
    override val eventsDb: Flow<List<Event>>
        get() = _eventsDb

    override suspend fun getEvents() {
        try {

            val response = apiService.getEvents()

            if (!response.isSuccessful) {
                when (response.code()) {
                    403 -> throw ApiError403(response.code().toString())
                    else -> throw ApiError(response.code(), response.message())
                }
            }
            val events = response.body() ?: throw ApiError(response.code(), response.message())
            val _events = events.map {
                if (AuthViewModel.myID == it.authorId) {
                    it.copy(eventOwner = true)
                } else it
            }
            daoEvents.insertAllEvents(
                _events.toEntity()
            )

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: ApiError403) {
            throw ApiError403("403")
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeEvent(id: Long, like: Boolean) {
        try {
            if (like) {


                val response = apiService.likeEventId(id)

                if (!response.isSuccessful) {
                    when (response.code()) {
                        403 -> throw ApiError403(response.code().toString())
                        404 -> throw ApiError404(response.code().toString())
                        else -> throw ApiError(response.code(), response.message())
                    }
                }
                val event = response.body() ?: throw ApiError(response.code(), response.message())
                daoEvents.insertEvent(
                    EventEntity.fromDto(event)
                )
            } else dislikeEvent(id)

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: ApiError403) {
            throw ApiError403("403")
        } catch (e: ApiError404) {
            throw ApiError404("404")
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun dislikeEvent(id: Long) {
        try {
            val response = apiService.dislikeEventId(id)

            if (!response.isSuccessful) {
                when (response.code()) {
                    403 -> throw ApiError403(response.code().toString())
                    404 -> throw ApiError404(response.code().toString())
                    else -> throw ApiError(response.code(), response.message())
                }
            }
            val event = response.body() ?: throw ApiError(response.code(), response.message())
            daoEvents.insertEvent(
                EventEntity.fromDto(event)
            )

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: ApiError403) {
            throw ApiError403("403")
        } catch (e: ApiError404) {
            throw ApiError404("404")
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveEvent(event: Event) {
        try {
            val response = apiService.sendEvent(event)

            if (!response.isSuccessful) {
                when (response.code()) {
                    403 -> throw ApiError403(response.code().toString())
                    404 -> throw ApiError404(response.code().toString())
                    else -> throw ApiError(response.code(), response.message())
                }
            }
            val event = response.body() ?: throw ApiError(response.code(), response.message())
            daoEvents.insertEvent(
                EventEntity.fromDto(event)
            )

        } catch (e: ApiError403) {
            println("EXC 403")
            throw ApiError403("403")
        } catch (e: Exception) {
            println("EXC ___${e.javaClass.name}")
            throw UnknownError
        }
    }

    override suspend fun deleteEvent(event: Event) {
        try {

            daoEvents.removeEventById(event.id!!)
            val response = apiService.removeEvent(event.id)
            if (!response.isSuccessful) {
                println("!response.isSuccessful")
                daoEvents.insertEvent(EventEntity.fromDto(event))
                when (response.code()) {
                    403 -> throw ApiError403(response.code().toString())
                    else -> throw ApiError(response.code(), response.message())
                }
            }

//            val post = response.body() ?: throw ApiError(response.code(), response.message())

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: ApiError403) {
            throw ApiError403("403")
        } catch (e: ApiError404) {
            throw ApiError404("404")
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun participateEvent(id: Long, status: Boolean) {
        try {
            if (status) {

                val response = apiService.participantsId(id)
                if (!response.isSuccessful) {
                    when (response.code()) {
                        403 -> throw ApiError403(response.code().toString())
                        404 -> throw ApiError404(response.code().toString())
                        else -> throw ApiError(response.code(), response.message())
                    }
                }
                val event = response.body() ?: throw ApiError(response.code(), response.message())
                daoEvents.insertEvent(
                    EventEntity.fromDto(event)
                )
            } else delParticipateEvent(id)

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: ApiError403) {
            throw ApiError403("403")
        } catch (e: ApiError404) {
            throw ApiError404("404")
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun delParticipateEvent(id: Long) {
        try {
            val response = apiService.delParticipantsId(id)

            if (!response.isSuccessful) {
                when (response.code()) {
                    403 -> throw ApiError403(response.code().toString())
                    404 -> throw ApiError404(response.code().toString())
                    else -> throw ApiError(response.code(), response.message())
                }
            }
            val event = response.body() ?: throw ApiError(response.code(), response.message())
            daoEvents.insertEvent(
                EventEntity.fromDto(event)
            )

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: ApiError403) {
            throw ApiError403("403")
        } catch (e: ApiError404) {
            throw ApiError404("404")
        } catch (e: Exception) {
            throw UnknownError
        }
    }

}