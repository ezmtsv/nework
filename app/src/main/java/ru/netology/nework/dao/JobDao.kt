package ru.netology.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.JobEntity


@Dao
interface JobDao {
    @Query("SELECT * FROM JobEntity WHERE idUser = :id ORDER BY id DESC")
    fun getAllJobs(id: Long): Flow<List<JobEntity>>

    @Query("SELECT * FROM JobEntity ORDER BY id DESC")
    fun getJobs(): Flow<List<JobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllJob(jobs: List<JobEntity>)

    @Query("DELETE FROM JobEntity WHERE id = :id")
    suspend fun removeJobById(id: Long)

    @Query("DELETE FROM JobEntity")
    suspend fun removeJobs()
}