package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "EventRemoteKeyEntity")
data class EventRemoteKeyEntity(
    @PrimaryKey
    val type: KeyType,
    val id: Long?,
) {
    enum class KeyType {
        AFTER, BEFORE
    }
}