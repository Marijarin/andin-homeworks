package ru.netology.nmedia.repository


import androidx.paging.Pager
import androidx.paging.PagingConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.File
import javax.inject.Inject
import kotlin.random.Random


class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val apiService: ApiService,
    private val appAuth: AppAuth
) : PostRepository {
    override val data = Pager(
            config = PagingConfig( pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                PostPagingSource(apiService)
            }
    )
        .flow
    private val idDone = 1_000_000_000L
    override suspend fun getAll() {
        try {
            val response = apiService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val posts = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(posts.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun getNewerCount(newerPostId: Long): Flow<Int> = flow {
        while (true) {
            delay(120_000L)
            val response = apiService.getNewer(newerPostId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.toEntity(false))
            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)


    override suspend fun save(post: Post) {
        try {
            val response = apiService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val newPost = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(newPost))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {

            val response = apiService.removeById(id)
            postDao.removeById(id)
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

            val response = apiService.likeById(id)
            postDao.likeById(id)
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

            val response = apiService.unlikeById(id)
            postDao.likeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw  UnknownError
        }
    }

    override suspend fun update() {
        try {
            postDao.update()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw  UnknownError
        }
    }

    private suspend fun upload(file: File): Media {
        try {
            val data = MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody()
            )
            val response = apiService.upload(data)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }

    }

    override suspend fun saveWithAttachment(post: Post, file: File) {
        try {
            val upload = upload(file)
            val postWithAttachment = post.copy(attachment = Attachment(upload.id))
            save(postWithAttachment)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw  UnknownError
        }
    }

}
