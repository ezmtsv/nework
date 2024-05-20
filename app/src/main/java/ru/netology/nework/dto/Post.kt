package ru.netology.nework.dto

data class Post(
    val id: Long,
    val authorId: Long,
    val author: String? = null,
    val authorJob: String? = null,
    val authorAvatar: String? = null,
    val content: String? = null,
    val published: String? = null,
    val link: String? = null,
    val mentionIds: List<Long>? = null,
    val mentionedMe: Boolean = false,
    val likeOwnerIds: List<Long>? = null,
    val likedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val coords: Coordinates? = null,
    val users: Map<String, UserPreview>? = null,
    val postOwner: Boolean = false
)
