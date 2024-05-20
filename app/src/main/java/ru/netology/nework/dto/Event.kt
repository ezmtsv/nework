package ru.netology.nework.dto


import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("attachment")
    val attachment: Attachment,
    @SerializedName("author")
    val author: String,
    @SerializedName("authorAvatar")
    val authorAvatar: String,
    @SerializedName("authorId")
    val authorId: Long,
    @SerializedName("authorJob")
    val authorJob: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("coordinates")
    val coordinates: Coordinates,
    @SerializedName("datetime")
    val datetime: String,
    @SerializedName("id")
    val id: Long,
    @SerializedName("likeOwnerIds")
    val likeOwnerIds: List<Long>,
    @SerializedName("likedByMe")
    val likedByMe: Boolean,
    @SerializedName("link")
    val link: String,
    @SerializedName("participantsIds")
    val participantsIds: List<Long>,
    @SerializedName("participatedByMe")
    val participatedByMe: Boolean,
    @SerializedName("published")
    val published: String,
    @SerializedName("speakerIds")
    val speakerIds: List<Long>,
    @SerializedName("type")
    val type: String,
    @SerializedName("users")
    val users: Map<String, UserPreview>,
)