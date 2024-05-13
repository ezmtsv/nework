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
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.UserPreview
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.repository.UsersRepository
import javax.inject.Inject


@HiltViewModel
@ExperimentalCoroutinesApi
class UsersViewModel @Inject constructor(
    private val usersRepo: UsersRepository
) : ViewModel() {

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

//    private val _listUsers = MutableLiveData<List<UserResponse>>()
    val listUsers: LiveData<List<UserResponse>> = usersRepo.allUsers.asLiveData(Dispatchers.Default)


    private val _userAccount: MutableLiveData<UserResponse> = MutableLiveData()
    val userAccount: LiveData<UserResponse>
        get() = _userAccount


    val userJobs: LiveData<List<Job>> = usersRepo.allUsersJob.asLiveData(Dispatchers.Default)


    init {
        loadUsers()
    }

    fun loadUsers() {
        _dataState.value = FeedModelState(loading = true)
        viewModelScope.launch {
            try {
                usersRepo.getUsers()
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                if (e.javaClass.name == "ru.netology.nework.error.ApiError403") {
                    _dataState.value = FeedModelState(error403 = true)
                } else _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun getUserJobs(id: Long) {
        _dataState.value = FeedModelState(loading = true)
        viewModelScope.launch {
            try {
                usersRepo.getJobs(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                if (e.javaClass.name == "ru.netology.nework.error.ApiError403") {
                    _dataState.value = FeedModelState(error403 = true)
                } else _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun getUser(id: Long) {
        _dataState.value = FeedModelState(loading = true)
        viewModelScope.launch {
            try {
                usersRepo.getUser(id)
            } catch (e: Exception) {

                if (e.javaClass.name == "ru.netology.nework.error.ApiError404") {
                    _dataState.value = FeedModelState(error404 = true)
//                    println(e.javaClass.name)
                } else _dataState.value = FeedModelState(error = true)
            }

        }
    }

    fun takeUser(user: UserResponse?) {
        user?.let {
            _userAccount.value = it
        }

    }
}