package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post
import java.lang.Exception

interface PostRepository {
    fun getAll(callback: Callback <List<Post>>){}
    fun likeById(id: Long, callback: Callback <Post>)
    fun unlikeById(id: Long, callback: Callback <Post>)
    fun save(post: Post, callback: Callback <Post>)
    fun removeById(id: Long, callback: Callback <Unit>)

    interface Callback <T>{
        fun onSuccess(value: T){}
        fun onError(e: Exception){}
    }
}
