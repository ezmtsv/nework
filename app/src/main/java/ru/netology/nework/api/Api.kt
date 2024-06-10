package ru.netology.nework.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.User
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.media.Media

interface ApiService {
    // one user (Wall)
    @GET("/api/{authorId}/wall")
    suspend fun getPostsUser(@Path("authorId") authorId: Long): Response<List<Post>>
//
//    @GET("/api/{authorId}/wall/{id}/newer")
//    suspend fun getPostsUserNewer(
//        @Path("authorId") authorId: Long,
//        @Path("id") id: Long
//    ): Response<List<Post>>
//
//    @GET("/api/{authorId}/wall/{id}/before")
//    suspend fun getPostsUserBefore(
//        @Path("authorId") authorId: Long,
//        @Path("id") id: Long,
//        @Query("count") count: Int,
//    ): Response<List<Post>>
//
//    @GET("/api/{authorId}/wall/{id}/after")
//    suspend fun getPostsUserAfter(
//        @Path("authorId") authorId: Long,
//        @Path("id") id: Long,
//        @Query("count") count: Int,
//    ): Response<List<Post>>
//
//    @GET("/api/{authorId}/wall/{id}")
//    suspend fun getPostsUserId(
//        @Path("authorId") authorId: Long,
//        @Path("id") id: Long
//    ): Response<Post>
//
//    @GET("/api/{authorId}/wall/latest")
//    suspend fun getPostsUserLatest(
//        @Path("authorId") authorId: Long,
//        @Query("count") count: Int,
//    ): Response<List<Post>>

//    @POST("/api/{authorId}/wall/{id}/likes")
//    suspend fun likeById(@Path("id") id: Long): Response<Post>
//
//    @DELETE("/api/{authorId}/wall/{id}/likes")
//    suspend fun removeById(@Path("id") id: Long): Response<Post>


//    //My Wall
//    @GET("/api/my/wall")
//    suspend fun getMyWall(): Response<List<Post>>
//
//    @GET("/api/my/wall/{id}/newer")
//    suspend fun getMyWallNewer(@Path("id") id: Long): Response<List<Post>>
//
//    @GET("/api/my/wall/{id}/before")
//    suspend fun getMyPostsBefore(
//        @Path("id") id: Long,
//        @Query("count") count: Int,
//    ): Response<List<Post>>
//
//    @GET("/api/my/wall/{id}/after")
//    suspend fun getMyPostsAfter(
//        @Path("id") id: Long,
//        @Query("count") count: Int,
//    ): Response<List<Post>>
//
//    @GET("/api/my/wall/{id}")
//    suspend fun getPostsUserId(
//        @Path("id") id: Long
//    ): Response<Post>
//
//    @GET("/api/my/wall/latest")
//    suspend fun getMyPostsLatest(
//        @Query("count") count: Int,
//    ): Response<List<Post>>
//
//    @POST("/api/my/wall/{id}/likes")
//    suspend fun likeMyById(@Path("id") id: Long): Response<Post>
//
//    @DELETE("/api/my/wall/{id}/likes")
//    suspend fun removeMyById(@Path("id") id: Long): Response<Post>


    // Users
    @GET("/api/users")
    suspend fun getUsers(): Response<List<UserResponse>>

    @GET("/api/users/{id}")
    suspend fun getUser(@Path("id") id: Long): Response<UserResponse>

    @Multipart
    @POST("/api/users/registration")
    suspend fun userReg(
        @Query("login") login: String,
        @Query("pass") pass: String,
        @Query("name") name: String,
        @Part media: MultipartBody.Part,
    ): Response<User>

    @POST("/api/users/authentication")
    suspend fun userAuth(
        @Query("login") login: String,
        @Query("pass") pass: String,
    ): Response<User>


    // Posts
    @GET("/api/posts")
    suspend fun getPosts(): Response<List<Post>>

//    @GET("/api/posts/{id}/newer")
//    suspend fun getPostsNewer(@Path("id") id: Long): Response<List<Post>>

    @GET("/api/posts/{id}/before")
    suspend fun getPostsBefore(
        @Path("id") id: Long,
        @Query("count") count: Int,
    ): Response<List<Post>>

    @GET("/api/posts/{id}/after")
    suspend fun getPostsAfter(
        @Path("id") id: Long,
        @Query("count") count: Int,
    ): Response<List<Post>>

//    @GET("/api/posts/{id}")
//    suspend fun getPostsId(
//        @Path("id") id: Long
//    ): Response<Post>

    @GET("/api/posts/latest")
    suspend fun getPostsLatest(
        @Query("count") count: Int,
    ): Response<List<Post>>

    @POST("/api/posts/{id}/likes")
    suspend fun likePostsId(@Path("id") id: Long): Response<Post>

    @DELETE("/api/posts/{id}/likes")
    suspend fun dislikePostsId(@Path("id") id: Long): Response<Post>

    @DELETE("/api/posts/{id}")
    suspend fun removePost(@Path("id") id: Long): Response<Int>

    @POST("/api/posts")
    suspend fun sendPost(
        @Body post: Post
    ): Response<Post>

    // Jobs
    @GET("api/{userId}/jobs")
    suspend fun getUserIdJobs(@Path("userId") id: Long): Response<List<Job>>

    @POST("/api/my/jobs")
    suspend fun sendMyJob(
        @Body job: Job
    ): Response<Job>

    @DELETE("/api/my/jobs/{id}")
    suspend fun deleteMyJobs(@Path("id") id: Long): Response<Int>

    //Media
    @Multipart
    @POST("/api/media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>

    // Events
    @GET("/api/events")
    suspend fun getEvents(): Response<List<Event>>

    @POST("/api/events/{id}/likes")
    suspend fun likeEventId(@Path("id") id: Long): Response<Event>

    @DELETE("/api/events/{id}/likes")
    suspend fun dislikeEventId(@Path("id") id: Long): Response<Event>

    @DELETE("/api/events/{id}")
    suspend fun removeEvent(@Path("id") id: Long): Response<Int>

    @POST("/api/events/{id}/participants")
    suspend fun participantsId(@Path("id") id: Long): Response<Event>

    @DELETE("/api/events/{id}/participants")
    suspend fun delParticipantsId(@Path("id") id: Long): Response<Event>

    @POST("/api/events")
    suspend fun sendEvent(
        @Body event: Event
    ): Response<Event>

    @GET("/api/events/{id}/before")
    suspend fun getEventsBefore(
        @Path("id") id: Long,
        @Query("count") count: Int,
    ): Response<List<Event>>

    @GET("/api/events/{id}/after")
    suspend fun getEventsAfter(
        @Path("id") id: Long,
        @Query("count") count: Int,
    ): Response<List<Event>>

    @GET("/api/events/latest")
    suspend fun getEventsLatest(
        @Query("count") count: Int,
    ): Response<List<Event>>

}