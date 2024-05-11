package ru.netology.nework.dto

import com.google.gson.annotations.SerializedName

data class Users (
    @SerializedName("15")
    val additionalProp1: UserPreview?,
    @SerializedName("56")
    val additionalProp2: UserPreview?,
    @SerializedName("33")
    val additionalProp3: UserPreview?,
)



