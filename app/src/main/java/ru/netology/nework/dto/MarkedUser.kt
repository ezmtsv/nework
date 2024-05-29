package ru.netology.nework.dto

import javax.inject.Singleton


data class MarkedUser(
    val id: Long? = null,
    val marked: Boolean = true
)

@Singleton
object ListMarkedUsers {
    val listUsers: MutableList<MarkedUser> = mutableListOf()
    fun addUser(user: MarkedUser) {
        if (listUsers.find { it.id == user.id } == null) listUsers.add(user)
    }

    fun removeUser(user: MarkedUser) {
        if (listUsers.find { it.id == user.id } != null)listUsers.remove(user)
    }

    fun cleanUsers() {
        listUsers.clear()
    }
}