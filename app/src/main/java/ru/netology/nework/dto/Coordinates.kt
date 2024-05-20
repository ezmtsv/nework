package ru.netology.nework.dto

import com.google.gson.annotations.SerializedName


data class Coordinates(
    @SerializedName("lat")
    val lat: Double? = null,
    @SerializedName("long")
    val longCr: Double? = null,
)