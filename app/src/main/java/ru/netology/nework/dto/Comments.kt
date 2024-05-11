package ru.netology.nework.dto


import com.google.gson.annotations.SerializedName

data class Comments(
    @SerializedName("author")
    val author: String,
    @SerializedName("authorAvatar")
    val authorAvatar: String,
    @SerializedName("authorId")
    val authorId: Long,
    @SerializedName("content")
    val content: String,
    @SerializedName("id")
    val id: Long,
    @SerializedName("likeOwnerIds")
    val likeOwnerIds: List<Int>,
    @SerializedName("likedByMe")
    val likedByMe: Boolean,
    @SerializedName("postId")
    val postId: Long,
    @SerializedName("published")
    val published: String
)