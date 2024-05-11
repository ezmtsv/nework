package ru.netology.nework.model

import ru.netology.nework.dto.Post


data class FeedModel(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false,
)