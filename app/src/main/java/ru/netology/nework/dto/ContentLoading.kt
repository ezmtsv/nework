package ru.netology.nework.dto

import okhttp3.MultipartBody

data class ContentLoading (
    val partBody: MultipartBody.Part? = null,
    val length: Int? = null,
    val name: String? = null,
)