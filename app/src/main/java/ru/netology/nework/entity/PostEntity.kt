package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.UserPreview


@Entity(tableName = "PostEntity")
data class PostEntity(
    @PrimaryKey
    val id: Long,
    val authorId: Long,
    val author: String?,
    val authorJob: String?,
    val authorAvatar: String?,
    val content: String?,
    val published: String?,
    val link: String?,
    val mentionIds: List<Long>? = null,
    val mentionedMe: Boolean,
    val likeOwnerIds: List<Long>? = null,
    val likedByMe: Boolean,
    @Embedded
    val attachment: Attachment?,
    @Embedded
    val coords: Coordinates? = null,
//    @Embedded
    val users: Map<String, UserPreview>?,
    val postOwner: Boolean,
) {
    fun toDto() = Post(
        id,
        authorId,
        author,
        authorJob,
        authorAvatar,
        content,
        published,
        link,
        mentionIds,
        mentionedMe,
        likeOwnerIds,
        likedByMe,
        attachment,
        coords,
        users,
        postOwner,
    )

    companion object {
        fun fromDto(dto: Post) = PostEntity(
            dto.id,
            dto.authorId,
            dto.author,
            dto.authorJob,
            dto.authorAvatar,
            dto.content,
            dto.published,
            dto.link,
            dto.mentionIds,
            dto.mentionedMe,
            dto.likeOwnerIds,
            dto.likedByMe,
            dto.attachment,
            dto.coords,
            dto.users,
            dto.postOwner,
        )
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)