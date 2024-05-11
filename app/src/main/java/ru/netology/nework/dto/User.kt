package ru.netology.nework.dto


import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Long,
    @SerializedName("token")
    val token: String
)