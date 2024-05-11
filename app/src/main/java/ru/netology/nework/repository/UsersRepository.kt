package ru.netology.nework.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.UserResponse

interface UsersRepository {
    val allUsers: Flow<List<UserResponse>>
//    val userJob: Flow<List<Job>>
    val allUsersJob: Flow<List<Job>>
    suspend fun getUsers()
    suspend fun getUser(id: Long): UserResponse
    suspend fun getJobs(id: Long)
}