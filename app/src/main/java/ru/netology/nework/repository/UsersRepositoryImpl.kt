package ru.netology.nework.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.JobDao
import ru.netology.nework.dao.UserDao
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.entity.JobEntity
import ru.netology.nework.entity.UserResponseEntity
import ru.netology.nework.entity.toDto
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.ApiError400
import ru.netology.nework.error.ApiError403
import ru.netology.nework.error.ApiError404
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class UsersRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val daoUser: UserDao,
    private val daoJob: JobDao,
) : UsersRepository {

    private val _allUsersJob = daoJob.getJobs().map(List<JobEntity>::toDto)
    override val allUsersJob: Flow<List<Job>>
        get() = _allUsersJob


    private val _allUsers = daoUser.getAllUsers().map(List<UserResponseEntity>::toDto)
    override val allUsers: Flow<List<UserResponse>>
        get() = _allUsers

//    private var _userJob = MutableStateFlow<List<Job>>(emptyList<Job>())
//    override val userJob: Flow<List<Job>>
//        get() = _userJob

    override suspend fun getUsers() {
        try {
            val response = apiService.getUsers()
            if (!response.isSuccessful) {
                when (response.code()) {
                    403 -> throw ApiError403(response.code().toString())
                    else -> throw ApiError(response.code(), response.message())
                }
            }
            val users = response.body() ?: throw ApiError(response.code(), response.message())
            daoUser.insertAllUser(
                users.toEntity()
            )
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: ApiError403) {
            throw ApiError403("403")
        } catch (e: Exception) {
            throw UnknownError
        }

    }

    override suspend fun getUser(id: Long): UserResponse {
        try {
            val response = apiService.getUser(id)
            if (!response.isSuccessful) {
                when (response.code()) {
                    404 -> throw ApiError404(response.code().toString())
                    else -> throw ApiError(response.code(), response.message())
                }
            }

            val user = response.body() ?: throw ApiError(response.code(), response.message())
            daoUser.insertUser(UserResponseEntity.fromDto(user))
            return user

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: ApiError404) {
            throw ApiError404("404")
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getJobs(id: Long) {
        try {
            val response = apiService.getUserIdJobs(id)
            val jobs = response.body() ?: throw ApiError(response.code(), response.message())
            val _jobs = jobs.map { job -> job.copy(idUser = id) }
            daoJob.insertAllJob(
                _jobs.toEntity()
            )

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveJob(job: Job) {
        try {
            val response = apiService.sendMyJob(job)

            if (!response.isSuccessful) {
                when (response.code()) {
                    403 -> throw ApiError403(response.code().toString())
                    404 -> throw ApiError404(response.code().toString())
                    400 -> throw ApiError400(response.code().toString())
                    else -> throw ApiError(response.code(), response.message())
                }
            }
            val getJob = response.body() ?: throw ApiError(response.code(), response.message())
            daoJob.insertJob(
                JobEntity.fromDto(getJob)
            )

        } catch (e: ApiError403) {
            throw ApiError403("403")
        } catch (e: ApiError404) {
            throw ApiError403("404")
        } catch (e: ApiError400) {
            throw ApiError400("400")
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun deleteJob(job: Job) {
        try {

            job.id?.let {
                daoJob.removeJobById(it)
                val response = apiService.deleteMyJobs(job.id)
                if (!response.isSuccessful) {
                    println("!response.isSuccessful")
                    daoJob.insertJob(JobEntity.fromDto(job))
                    when (response.code()) {
                        403 -> throw ApiError403(response.code().toString())
                        404 -> throw ApiError404(response.code().toString())
                        else -> throw ApiError(response.code(), response.message())
                    }
                }
            }

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

//    interface GetMentionUser {
//        fun getUser(user: UserResponse)
//    }
//
//    override suspend fun getMentionsUsers(userId: Long, mention: GetMentionUser) {
//        try {
//            CoroutineScope(Dispatchers.Default).launch {
//
//                val user = daoUser.getUser(userId).map { it.toDto() }
//                user.flowOn(Dispatchers.IO).collect {
//                    mention.getUser(it)
//                    this.cancel()
//                }
//            }
//        } catch (e: Exception) {
//            throw DbError
//        }
//    }

}