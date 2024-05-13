package ru.netology.nework.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.User
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.toDto
import ru.netology.nework.entity.toEntity
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.error.*
import ru.netology.nework.media.Media
import ru.netology.nework.media.MediaUpload
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.AuthViewModel.Companion.myID
import java.io.IOException
import javax.inject.Inject


class PostsRepositoryImp @Inject constructor(
    private val apiService: ApiService,
    private val dao: PostDao,

    ) : PostsRepository {
    //    private val data: MutableLiveData<List<Post>> = MutableLiveData()
    private val _posts = dao.getAllPosts().map(List<PostEntity>::toDto)
    override val postsBd: Flow<List<Post>>
        get() = _posts

//    private val _userWall: MutableLiveData<List<Post>> = MutableLiveData()
//    override val userWall: LiveData<List<Post>>
//        get() = _userWall

//    private var userId = 0L
//    private val _userWall = dao.getUserWall(userId).map(List<PostEntity>::toDto)

//    private val _userWall = MutableStateFlow<List<Post>>(emptyList())
//    override val userWall: Flow<List<Post>>
//        get() = _userWall

    override suspend fun getPosts() {
        try {

//            val response = apiService.getPostsBefore(50, 20)
            val response = apiService.getPosts()

            if (!response.isSuccessful) {
                when (response.code()) {
                    403 -> throw ApiError403(response.code().toString())
                    else -> throw ApiError(response.code(), response.message())
                }
            }
            val posts = response.body() ?: throw ApiError(response.code(), response.message())
            val _posts = posts.map {
                if (myID == it.authorId) {
                    it.copy(postOwner = true)
                } else it
            }
//            _posts.forEach { println("${it.authorId} ${it.postOwner}  $myID") }
            dao.insert(
                _posts.toEntity()
            )

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: ApiError403) {
            throw ApiError403("403")
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getPostDb(id: Long): Post =
        try {
            dao.getPostById(id).single().toDto()
        } catch (e: Exception) {
            throw DbError
        }

    override suspend fun getUserPosts(id: Long) {
        try {

            val response = apiService.getPostsUser(id)

            if (!response.isSuccessful) {
                when (response.code()) {
                    403 -> throw ApiError403(response.code().toString())
                    else -> throw ApiError(response.code(), response.message())
                }
            }
            val posts = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(
                posts.toEntity()
            )


            //           _userWall.value = response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: ApiError403) {
            throw ApiError403("403")
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likePost(id: Long, like: Boolean) {
        try {
            if (like) {


                val response = apiService.likePostsId(id)

                if (!response.isSuccessful) {
                    when (response.code()) {
                        403 -> throw ApiError403(response.code().toString())
                        404 -> throw ApiError404(response.code().toString())
                        else -> throw ApiError(response.code(), response.message())
                    }
                }
                val post = response.body() ?: throw ApiError(response.code(), response.message())
                dao.insert(
                    PostEntity.fromDto(post)
                )
            } else dislikePost(id)

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: ApiError403) {
            throw ApiError403("403")
        } catch (e: ApiError404) {
            throw ApiError404("404")
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun dislikePost(id: Long) {
        try {
            val response = apiService.dislikePostsId(id)

            if (!response.isSuccessful) {
                when (response.code()) {
                    403 -> throw ApiError403(response.code().toString())
                    404 -> throw ApiError404(response.code().toString())
                    else -> throw ApiError(response.code(), response.message())
                }
            }
            val post = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(
                PostEntity.fromDto(post)
            )

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: ApiError403) {
            throw ApiError403("403")
        } catch (e: ApiError404) {
            throw ApiError404("404")
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun userAuth(login: String, pass: String): User {
        try {
            val response = apiService.userAuth(login, pass)
            if (!response.isSuccessful) {
                when (response.code()) {
                    400 -> throw ApiError400(response.code().toString())
                    404 -> throw ApiError404(response.code().toString())
                    else -> throw ApiError(response.code(), response.message())
                }
            }
            return response.body() ?: throw ApiError(response.code(), response.message())

        } catch (e: ApiError400) {
            println("400")
            throw ApiError400("400")
        } catch (e: ApiError404) {
            println("404")
            throw ApiError404("404")
        } catch (e: Exception) {
            println("EXC")
            throw UnknownError
        }
    }

    override suspend fun userReg(
        login: String,
        pass: String,
        name: String,
        upload: MediaUpload
    ): User {

        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file?.name, upload.file?.asRequestBody()!!
            )
            val response = apiService.userReg(login, pass, name, media)
            if (!response.isSuccessful) {
                when (response.code()) {
                    403 -> throw ApiError403(response.code().toString())
                    415 -> throw ApiError415(response.code().toString())
                    else -> throw ApiError(response.code(), response.message())
                }
            }
//
//                val user = response.body() ?: throw ApiError(response.code(), response.message())
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: ApiError403) {
            throw ApiError403("403")
        } catch (e: ApiError415) {
            throw ApiError415("415")
        } catch (e: Exception) {
            throw UnknownError
        }

    }

    override suspend fun savePost(post: Post) {
        try {
            val response = apiService.sendPost(post)

            if (!response.isSuccessful) {
                when (response.code()) {
                    403 -> throw ApiError403(response.code().toString())
                    404 -> throw ApiError404(response.code().toString())
                    else -> throw ApiError(response.code(), response.message())
                }
            }
            val getPost = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(
                PostEntity.fromDto(getPost.copy(postOwner = true))
            )

        } catch (e: ApiError403) {
            println("EXC 403")
            throw ApiError403("403")
        } catch (e: Exception) {
            println("EXC ___${e.javaClass.name}")
            throw UnknownError
        }
    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file?.name, upload.file?.asRequestBody()!!
            )
            println("upload media ${media.headers}")
            val response = apiService.upload(media)
            if (!response.isSuccessful) {
                when (response.code()) {
                    403 -> throw ApiError403(response.code().toString())
                    415 -> throw ApiError415(response.code().toString())
                    else -> throw ApiError(response.code(), response.message())
                }
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: ApiError403) {
            throw ApiError403("403")
        } catch (e: ApiError415) {
            throw ApiError415("415")
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun deletePost(post: Post) {
        try {

            dao.removeById(post.id)
            val response = apiService.removePost(post.id)
            if (!response.isSuccessful) {
                println("!response.isSuccessful")
                dao.insert(PostEntity.fromDto(post))
                when (response.code()) {
                    403 -> throw ApiError403(response.code().toString())
                    else -> throw ApiError(response.code(), response.message())
                }
            }

//            val post = response.body() ?: throw ApiError(response.code(), response.message())

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: ApiError403) {
            throw ApiError403("403")
        } catch (e: ApiError404) {
            throw ApiError404("404")
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun signOut() {
        try {
            CoroutineScope(Dispatchers.Default).launch {
                _posts.flowOn(Dispatchers.IO).collect {
                    val posts = it.map { post ->
                        post.copy(postOwner = false)
                    }
                    dao.insert(posts.toEntity())
                    this.cancel()
                }

            }
        } catch (e: Exception) {
            throw DbError
        }
    }

}