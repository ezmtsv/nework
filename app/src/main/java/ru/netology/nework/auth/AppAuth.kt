package ru.netology.nework.auth

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nework.dto.UserResponse
import javax.inject.Inject
import javax.inject.Singleton

private const val MY_ID = "myId"
private const val LOGIN = "login"
private const val PASSWORD = "password"
private const val TOKEN = "token"
private const val MY_ACC = "my_acc"

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _authState = MutableStateFlow(
        AuthState(
            prefs.getLong(MY_ID, 0),
            prefs.getString(LOGIN, null),
            prefs.getString(PASSWORD, null),
            prefs.getString(TOKEN, null),
        )
    )

    @Synchronized
    fun setAuth(myId: Long, login: String, pass: String, token: String) {
        _authState.value = AuthState(myId, login, pass, token)
        with(prefs.edit()) {
            putLong(MY_ID, myId)
            putString(LOGIN, login)
            putString(PASSWORD, pass)
            putString(TOKEN, token)
            commit()
        }
    }

    @Synchronized
    fun saveMyAcc(myAcc: UserResponse) {
        with(prefs.edit()) {
            putString(MY_ACC, Gson().toJson(myAcc))
            commit()
        }
    }

    fun getMyAcc(): UserResponse {
        val type = object : TypeToken<UserResponse?>() {}.type
        return Gson().fromJson(prefs.getString(MY_ACC, null), type)

    }

    @Synchronized
    fun removeAuth() {
        _authState.value = AuthState()
        with(prefs.edit()) {
            clear()
            commit()
        }
    }

    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    data class AuthState(
        val myId: Long? = null,
        val login: String? = null,
        val pass: String? = null,
        val token: String? = null,
    )
}