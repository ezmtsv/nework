package ru.netology.nework.dto

import com.google.gson.annotations.SerializedName
import ru.netology.nework.enumeration.MeetingType


data class Event(
    val id: Long? = null,
    val authorId: Long? = null,
    val author: String? = null,
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String? = null,
    val datetime: String? = null,
    val likeOwnerIds: List<Long>? = null,
    val likedByMe: Boolean? = null,
    val link: String? = null,
    val participantsIds: List<Long>? = null,
    val participatedByMe: Boolean? = null,
    val published: String? = null,
    val speakerIds: List<Long>? = null,
    @SerializedName("type")
    val typeMeeting: MeetingType? = null,
    val users: Map<String, UserPreview>? = null,
    val attachment: Attachment? = null,
    val coords: Coordinates? = null,
    val eventOwner: Boolean = false
)