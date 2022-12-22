package ru.netology.nmedia.repository


import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okio.IOException
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError


class PostRepositoryImpl(private val postDao: PostDao) : PostRepository {
    override val data: Flow<List<Post>> = postDao.getAll()
        .map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)
    private val idDone = 1_000_000_000L
    override suspend fun getAll() {
        try {
            val response = PostsApi.retrofitService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val posts = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(posts.map { it.copy(saved = true) }.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun getNewerCount(newerPostId: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = PostsApi.retrofitService.getNewer(newerPostId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.map { it.copy(saved = true) }.toEntity(false))
            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)


    override suspend fun save(post: Post) {
        try {
            val idNew = idDone + (data.asLiveData().value?.size ?: 0)
            postDao.insert(PostEntity.fromDto(post.copy(id = idNew)))
            val response = PostsApi.retrofitService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val newPost = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.removeById(idNew)
            postDao.insert(PostEntity.fromDto(newPost.copy(saved = true)))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }

    }

    override suspend fun removeById(id: Long) {
        try {
            postDao.removeById(id)
            val response = PostsApi.retrofitService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            postDao.likeById(id)
            val response = PostsApi.retrofitService.likeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw  UnknownError
        }
    }

    override suspend fun unlikeById(id: Long) {
        try {
            postDao.likeById(id)
            val response = PostsApi.retrofitService.unlikeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw  UnknownError
        }
    }


    override suspend fun checkNotSaved() {
        data.asLiveData().value?.forEach {
            if (it.id >= idDone) {
                try {
                    val response = PostsApi.retrofitService.save(it.copy(id = 0L))
                    if (!response.isSuccessful) {
                        throw ApiError(response.code(), response.message())
                    }
                    val newPost =
                        response.body() ?: throw ApiError(response.code(), response.message())
                    postDao.removeById(it.id)
                    postDao.insert(PostEntity.fromDto(newPost.copy(saved = true)))
                } catch (e: IOException) {
                    throw NetworkError
                } catch (e: Exception) {
                    throw UnknownError
                }
            }
        }
    }

    override suspend fun update() {
        try{
        postDao.update()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw  UnknownError
        }
    }

}
