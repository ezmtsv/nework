package ru.netology.nework.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.EventEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM EventsEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, EventEntity>
    @Query("SELECT * FROM EventsEntity WHERE authorId = :id ORDER BY id DESC")
    fun getByAuthorEvents(id: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM EventsEntity ORDER BY id DESC")
    fun getEvents(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(events: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEvents(events: List<EventEntity>)

    @Query("DELETE FROM EventsEntity WHERE id = :id")
    suspend fun removeEventById(id: Long)

    @Query("DELETE FROM EventsEntity")
    suspend fun removeEvents()
}