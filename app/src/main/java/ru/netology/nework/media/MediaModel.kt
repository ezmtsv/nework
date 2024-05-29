package ru.netology.nework.media

import android.net.Uri

data class MediaModel(
    val uri: Uri? = null,
    val name: String? = null,
    val length: Int? = null,
    val type: String? = null,
)