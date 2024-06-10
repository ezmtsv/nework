package ru.netology.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.Event

interface EventsRepository {
    //    val eventsDb: Flow<List<Event>>
    val eventsDb: Flow<PagingData<Event>>
    val eventsFlow: Flow<List<Event>>
    suspend fun getEvents()
    suspend fun likeEvent(id: Long, like: Boolean)
    suspend fun saveEvent(event: Event)
    suspend fun deleteEvent(event: Event)
    suspend fun participateEvent(id: Long, status: Boolean)
    suspend fun getEventsDB()
}