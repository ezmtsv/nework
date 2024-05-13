package ru.netology.nework.dto


import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName("token")
    val token: String? = null
)