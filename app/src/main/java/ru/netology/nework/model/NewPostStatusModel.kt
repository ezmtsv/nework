package ru.netology.nework.model

import android.view.View
import ru.netology.nework.enumeration.AttachmentType

data class NewPostStatusModel (
    val groupUsers: Int = View.GONE,
    val groupImage: Int = View.GONE,
    val groupLoadFile: Int = View.GONE,
    val geo: Int = View.GONE,
    val statusCoords: Boolean = false,
    val statusLoading: Boolean = false,
)