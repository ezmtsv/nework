package ru.netology.nework.dto


import com.google.gson.annotations.SerializedName

data class Job(
    val id: Long?= null,
    val idUser: Long?= null,
    val name: String?= null,
    val position: String?= null,
    val start: String?= null,
    val finish: String?= null,
    val link: String? = null,
)

