package ru.netology.nework.dto


import com.google.gson.annotations.SerializedName
import ru.netology.nework.enumeration.AttachmentType

data class Attachment(
    val url: String,
    val attachmentType: AttachmentType?
)