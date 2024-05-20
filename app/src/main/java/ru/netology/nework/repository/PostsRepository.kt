package ru.netology.nework.repository

import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.User
import ru.netology.nework.media.Media
import ru.netology.nework.media.MediaUpload


interface PostsRepository {
    val postsBd: Flow<List<Post>>

    //    val userWall: Flow<List<Post>>
    suspend fun getPosts()
    suspend fun getPostDb(id: Long): Post
    suspend fun getUserPosts(id: Long)
    suspend fun likePost(id: Long, like: Boolean)

    //    suspend fun dislikePost(id: Long)
    suspend fun userAuth(login: String, pass: String): User?
    suspend fun userReg(login: String, pass: String, name: String, upload: MediaUpload): User?
    suspend fun savePost(post: Post)

    //    suspend fun upload(upload: MediaUpload): Media
    suspend fun upload(media: MultipartBody.Part): Media
    suspend fun deletePost(post: Post)
    suspend fun signOut()
}