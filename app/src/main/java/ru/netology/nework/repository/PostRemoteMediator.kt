package ru.netology.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.PostRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.PostRemoteKeyEntity
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.viewmodel.AuthViewModel
import java.lang.Exception
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator @Inject constructor(
    private val service: ApiService,
    private val db: AppDb,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
//                    println("REFRESH  postRemoteKeyDao.min() ${postRemoteKeyDao.min()}  postRemoteKeyDao.max() ${postRemoteKeyDao.max()}")
                    val id = postRemoteKeyDao.max()
                    if (id == null) {
                        service.getPostsLatest(state.config.initialLoadSize)
                    } else service.getPostsAfter(id, state.config.pageSize)
                }

                LoadType.PREPEND -> {
                    return MediatorResult.Success(false)
                }

                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    service.getPostsBefore(id, state.config.pageSize)

                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message(),
            )
            db.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        if (postRemoteKeyDao.isEmpty()) {
                            postRemoteKeyDao.clear()
                            postRemoteKeyDao.insert(
                                listOf(
                                    PostRemoteKeyEntity(
                                        type = PostRemoteKeyEntity.KeyType.AFTER,
                                        id = body.first().id,
                                    ),
                                    PostRemoteKeyEntity(
                                        type = PostRemoteKeyEntity.KeyType.BEFORE,
                                        id = body.last().id,
                                    ),
                                )
                            )
                        } else {
                            if (body.isNotEmpty()) {
                                postRemoteKeyDao.insert(
                                    listOf(
                                        PostRemoteKeyEntity(
                                            type = PostRemoteKeyEntity.KeyType.AFTER,
                                            id = body.first().id,
                                        )
                                    )
                                )
                            }
                        }
                    }

//                    LoadType.PREPEND -> {
//                        if (body.isNotEmpty()) {
//                            postRemoteKeyDao.insert(
//                                PostRemoteKeyEntity(
//                                    type = PostRemoteKeyEntity.KeyType.AFTER,
//                                    id = body.first().id,
//                                )
//                            )
//                        }
//                    }

                    LoadType.APPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.BEFORE,
                                id = body.last().id,
                            )
                        )
                    }

                    else -> {}
                }

                val posts = response.body() ?: throw ApiError(response.code(), response.message())
                val getPosts = posts.map {
                    if (AuthViewModel.myID == it.authorId) {
                        it.copy(postOwner = true)
                    } else it
                }
                postDao.insert(
                    getPosts.toEntity()
                )
            }

            return MediatorResult.Success(endOfPaginationReached = false)

        } catch (e: Exception) {
            println("EE ${e.javaClass.name}")
            return MediatorResult.Error(e)
        }
    }

}