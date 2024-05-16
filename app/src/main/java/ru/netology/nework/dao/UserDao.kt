package ru.netology.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.UserResponseEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM UserResponseEntity ORDER BY id DESC")
    fun getAllUsers(): Flow<List<UserResponseEntity>>

    @Query("SELECT * FROM UserResponseEntity WHERE id = :idUser")
    fun getUser(idUser: Long): Flow<UserResponseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserResponseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUser(users: List<UserResponseEntity>)

    @Query("DELETE FROM UserResponseEntity WHERE id = :id")
    suspend fun removeUserById(id: Long)

    @Query("DELETE FROM UserResponseEntity")
    suspend fun removeUsers()
}