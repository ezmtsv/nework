package ru.netology.nework.model

data class NewPostStatusModel (
    val btnUsers: Boolean = false,
    val btnMap : Boolean = false,
    val groupImage: Boolean = false,
    val groupLoading: Boolean = false,
    val groupSelectAttach: Boolean = false,
)