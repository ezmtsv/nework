package ru.netology.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dao.EventRemoteKeyDao
import ru.netology.nework.dao.JobDao
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.PostRemoteKeyDao
import ru.netology.nework.dao.UserDao
import ru.netology.nework.dto.UserPreview
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.EventRemoteKeyEntity
import ru.netology.nework.entity.JobEntity
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.PostRemoteKeyEntity
import ru.netology.nework.entity.UserResponseEntity
import java.lang.reflect.Type

@TypeConverters(
    DataConvertorList::class,
    DataConvertorAdditionalProp::class,
)
@Database(
    entities = [PostEntity::class,
        UserResponseEntity::class,
        JobEntity::class,
        EventEntity::class,
        PostRemoteKeyEntity::class,
        EventRemoteKeyEntity::class],
    version = 1
)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun jobDao(): JobDao
    abstract fun userDao(): UserDao
    abstract fun eventDao(): EventDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao
}

class DataConvertorList {
    private val listType: Type = object : TypeToken<List<Long>?>() {}.type

    @TypeConverter
    fun toGson(list: List<Long>?): String =
        Gson().toJson(list)

    @TypeConverter
    fun fromGson(str: String): List<Long>? =
        Gson().fromJson<List<Long>?>(str, listType)
}

class DataConvertorAdditionalProp {
    private val type = object : TypeToken<Map<String, UserPreview>?>() {}.type

    @TypeConverter
    fun toGson(obj: Map<String, UserPreview>?): String =
        Gson().toJson(obj)

    @TypeConverter
    fun fromGson(str: String): Map<String, UserPreview>? =
        Gson().fromJson<Map<String, UserPreview>?>(str, type)
}

