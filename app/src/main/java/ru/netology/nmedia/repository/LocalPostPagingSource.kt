package ru.netology.nmedia.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.error.AppError
import java.io.IOException
import kotlin.coroutines.coroutineContext

class LocalPostPagingSource(
    private val postDao: PostDao,

) : PagingSource<Long, Post>() {
    override fun getRefreshKey(state: PagingState<Long, Post>): Long? = null

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Post> {
        try {
            val result = when (params) {
                is LoadParams.Refresh -> {
                    postDao.getLatest(params.loadSize)
                }
                is LoadParams.Append -> {
                    postDao.getBefore(id = params.key, count = params.loadSize)
                }
                is LoadParams.Prepend -> return LoadResult.Page(
                    data = emptyList(), nextKey = null, prevKey = params.key
                )
            }.catch { e -> throw AppError.from(e) }
                .flowOn(Dispatchers.Default)


            val nextKey = if (result.count() == 0) null else result.last().last().id
            return LoadResult.Page(
                data = result.stateIn(CoroutineScope(context = coroutineContext)).value.toDto(),
                prevKey = params.key,
                nextKey = nextKey,
            )
        } catch (e: IOException) {
            return LoadResult.Error(e)
        }

    }

}
