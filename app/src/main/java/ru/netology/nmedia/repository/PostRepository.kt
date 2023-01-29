package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import java.io.File

interface PostRepository {
    val data: Flow<PagingData<Post>>
    //suspend fun getAll()
    //fun getNewerCount(newerPostId: Long): Flow<Int>
    suspend fun likeById(id: Long)
    suspend fun unlikeById(id: Long)
    suspend fun save(post: Post)
    suspend fun removeById(id: Long)
    suspend fun saveWithAttachment(post: Post, file: File)

}
