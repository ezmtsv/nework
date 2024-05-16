package ru.netology.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nework.dao.JobDao
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.UserDao
import ru.netology.nework.dto.UserPreview
import ru.netology.nework.entity.JobEntity
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.UserResponseEntity
import java.lang.reflect.Type

@TypeConverters(
    DataConvertorList::class,
    DataConvertorAdditionalProp::class,
//    DataConvertorUsers::class,
//    DataConvertorDouble::class,
)
@Database(
    entities = [PostEntity::class,
        UserResponseEntity::class,
        JobEntity::class],
    version = 1
)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun jobDao(): JobDao
    abstract fun userDao(): UserDao
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

//class DataConvertorAdditionalProp {
//    private val type = object : TypeToken<UserPreview?>() {}.type
//
//    @TypeConverter
//    fun toGson(obj: UserPreview?): String =
//        Gson().toJson(obj)
//
//    @TypeConverter
//    fun fromGson(str: String): UserPreview? =
//        Gson().fromJson<UserPreview?>(str, type)
//
//
//}

class DataConvertorAdditionalProp {
    private val type = object : TypeToken<Map<String, String>?>() {}.type

    @TypeConverter
    fun toGson(obj: Map<String, String>?): String =
        Gson().toJson(obj)

    @TypeConverter
    fun fromGson(str: String): Map<String, String>? =
        Gson().fromJson<Map<String, String>?>(str, type)


}

//class DataConvertorDouble {
//    private val type = object: TypeToken<Double?>() {}.type
//
//    @TypeConverter
//    fun toGson(obj: Double?): String =
//        Gson().toJson(obj)
//
//    @TypeConverter
//    fun fromGson(str: String): Double? =
//        Gson().fromJson<Double?>(str, type)
//
//
//}

//class DataConvertorUsers {
//    private val type = object : TypeToken<List<UserPreview>?>(){}.type
//
//    @TypeConverter
//    fun toGson(obj: List<UserPreview>?): String =
//        Gson().toJson(obj)
//
//    @TypeConverter
//    fun fromGson(str: String): List<UserPreview>? =
//        Gson().fromJson<List<UserPreview>?>(str, type)
//
//
//}

