package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Job

@Entity(tableName = "JobEntity")
data class JobEntity(
    @PrimaryKey
    val id: Long? = null,
    val idUser: Long? = null,
    val name: String? = null,
    val position: String? = null,
    val start: String? = null,
    val finish: String? = null,
    val link: String? = null,
) {
    fun toDto() = Job(
        id,
        idUser,
        name,
        position,
        start,
        finish,
        link,
    )

    companion object {
        fun fromDto(dto: Job) = JobEntity(
            dto.id,
            dto.idUser,
            dto.name,
            dto.position,
            dto.start,
            dto.finish,
            dto.link,
        )
    }
}

fun List<JobEntity>.toDto(): List<Job> = map(JobEntity::toDto)
fun List<Job>.toEntity(): List<JobEntity> = map(JobEntity::fromDto)