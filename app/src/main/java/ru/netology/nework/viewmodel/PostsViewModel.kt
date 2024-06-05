package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Post
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.media.Media
import ru.netology.nework.media.PhotoModel
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.repository.PostsRepository
import javax.inject.Inject


@HiltViewModel
@ExperimentalCoroutinesApi

class PostsViewModel @Inject constructor(
    private val repository: PostsRepository,
//    private val repoUsers: UsersRepository,
) : ViewModel() {

//    private val noPhoto = PhotoModel()
//    private val _photo = MutableLiveData(noPhoto)
//    val photo: LiveData<PhotoModel>
//        get() = _photo
//    private val _typeAttach = MutableLiveData<AttachmentType?>()
//    val typeAttach: LiveData<AttachmentType?>
//        get() = _typeAttach

    val data: LiveData<List<Post>> = repository.postsBd
        .asLiveData(Dispatchers.IO)

//    private val _location = MutableLiveData<Point>()
//    val location: LiveData<Point>
//        get() = _location
//
//    private val _newStatusViewsModel = MutableLiveData<StatusModelViews>()
//    val newStatusViewsModel: LiveData<StatusModelViews>
//        get() = _newStatusViewsModel

    private val _userWall = MutableLiveData<List<Post>>()
    val userWall: LiveData<List<Post>>
        get() = _userWall

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState


//    init {
//        loadPosts()
//    }

    fun loadPosts() {
        _dataState.value = FeedModelState(loading = true)
        viewModelScope.launch {
            try {
                repository.getPosts()
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                if (e.javaClass.name == "ru.netology.nework.error.ApiError403") {
                    _dataState.value = FeedModelState(error403 = true)
                } else if (e.javaClass.name == "ru.netology.nework.error.NetworkError")
                    _dataState.value = FeedModelState(errorNetWork = true)
            }
        }
    }

    fun getUserPosts(id: Long) {
        _dataState.value = FeedModelState(loading = true)
        viewModelScope.launch {
            try {
                repository.getUserPosts(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                if (e.javaClass.name == "ru.netology.nework.error.ApiError403") {
                    _dataState.value = FeedModelState(error403 = true)
                }
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun savePost(post: Post, media: MultipartBody.Part?, typeAttach: AttachmentType?) {
        _dataState.value = FeedModelState(loading = true)
        viewModelScope.launch {
            try {
                if (typeAttach != null && media != null) {
                    val _media: Media = repository.upload(media)
                    val postWithAttachment =
                        post.copy(attachment = Attachment(_media.url, typeAttach))
                    repository.savePost(postWithAttachment)
                } else {
                    println("Save POST $post")
                    repository.savePost(post)
                }
                _dataState.value = FeedModelState()

            } catch (e: Exception) {
                when (e.javaClass.name) {
                    "ru.netology.nework.error.ApiError403" -> {
                        _dataState.value = FeedModelState(error403 = true)
                    }

                    "ru.netology.nework.error.ApiError415" -> {
                        _dataState.value = FeedModelState(error415 = true)
                    }

                    else -> {
                        _dataState.value = FeedModelState(error = true)
                    }
                }
            }
        }
    }

//    fun changePhoto(uri: Uri?, file: File?) {
//        _photo.value = PhotoModel(uri, file)
//    }
//
//    fun clearPhoto() {
//        _photo.value = noPhoto
//    }

    fun like(post: Post, like: Boolean) {
        _dataState.value = FeedModelState(loading = true)
        println("post ${post.id}")
        viewModelScope.launch {
            try {
                repository.likePost(post.id, like)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                when (e.javaClass.name) {
                    "ru.netology.nework.error.ApiError403" -> {
                        _dataState.value = FeedModelState(error403 = true)
                    }

                    "ru.netology.nework.error.ApiError404" -> {
                        _dataState.value = FeedModelState(error404 = true)
                    }

                    else -> {
                        _dataState.value = FeedModelState(error = true)
                    }
                }
            }
        }

    }


    fun removePost(post: Post) {
        _dataState.value = FeedModelState(loading = true)
        viewModelScope.launch {
            try {
                repository.deletePost(post)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                when (e.javaClass.name) {
                    "ru.netology.nework.error.ApiError403" -> {
                        _dataState.value = FeedModelState(error403 = true)
                    }

                    "ru.netology.nework.error.ApiError404" -> {
                        _dataState.value = FeedModelState(error404 = true)
                    }

                    else -> {
                        _dataState.value = FeedModelState(error = true)
                    }
                }
            }
        }

    }

    fun takePosts(list: List<Post>?) {
        list?.let { _userWall.value = it }

    }

//    fun setTypeAttach(attach: AttachmentType?) {
//        _typeAttach.value = attach
//    }


//
//    fun setStatusViews(newStatus: StatusModelViews) {
//        _newStatusViewsModel.value = newStatus
//    }
//
//    fun setLocation(point: Point) {
//        _location.value = point
//    }


}