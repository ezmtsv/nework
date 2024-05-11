package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.UserResponse

@Entity(tableName = "UserResponseEntity")
data class UserResponseEntity(
    @PrimaryKey
    val id: Long? = null,
    val login: String? = null,
    val name: String? = null,
    val avatar: String? = null,
) {
    fun toDto() = UserResponse(
        id,
        login,
        name,
        avatar,
    )

    companion object {
        fun fromDto(dto: UserResponse) = UserResponseEntity(
            dto.id,
            dto.login,
            dto.name,
            dto.avatar,
        )
    }
}

fun List<UserResponseEntity>.toDto(): List<UserResponse> = map(UserResponseEntity::toDto)
fun List<UserResponse>.toEntity(): List<UserResponseEntity> = map(UserResponseEntity::fromDto)