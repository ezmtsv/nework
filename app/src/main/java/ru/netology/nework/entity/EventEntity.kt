package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.UserPreview
import ru.netology.nework.enumeration.MeetingType

@Entity(tableName = "EventsEntity")
data class EventEntity(
    @PrimaryKey
    val id: Long?,
    val authorId: Long?,
    val author: String?,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String?,
    val datetime: String?,
    val likeOwnerIds: List<Long>? = null,
    val likedByMe: Boolean?,
    val link: String?,
    val participantsIds: List<Long>? = null,
    val participatedByMe: Boolean?,
    val published: String?,
    val speakerIds: List<Long>? = null,
    @SerializedName("type")
    val typeMeeting: MeetingType?,
    val users: Map<String, UserPreview>?,
    @Embedded
    val attachment: Attachment?,
    @Embedded
    val coords: Coordinates? = null,
    val eventOwner: Boolean,
) {
    fun toDto() = Event(
        id,
        authorId,
        author,
        authorAvatar,
        authorJob,
        content,
        datetime,
        likeOwnerIds,
        likedByMe,
        link,
        participantsIds,
        participatedByMe,
        published,
        speakerIds,
        typeMeeting,
        users,
        attachment,
        coords,
        eventOwner,
    )

    companion object {
        fun fromDto(dto: Event) = EventEntity(
            dto.id,
            dto.authorId,
            dto.author,
            dto.authorAvatar,
            dto.authorJob,
            dto.content,
            dto.datetime,
            dto.likeOwnerIds,
            dto.likedByMe,
            dto.link,
            dto.participantsIds,
            dto.participatedByMe,
            dto.published,
            dto.speakerIds,
            dto.typeMeeting,
            dto.users,
            dto.attachment,
            dto.coords,
            dto.eventOwner
        )
    }
}

fun List<EventEntity>.toDto(): List<Event> = map(EventEntity::toDto)
fun List<Event>.toEntity(): List<EventEntity> = map(EventEntity::fromDto)