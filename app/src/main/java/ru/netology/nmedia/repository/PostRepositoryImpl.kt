package ru.netology.nmedia.repository

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dto.Post


class PostRepositoryImpl: PostRepository {

    override fun getAll(callback: PostRepository.Callback<List<Post>>) {
        PostsApi.retrofitService.getAll()
            .enqueue(object: Callback <List<Post>> {
                override fun onResponse(call: Call <List<Post>>, response: Response<List<Post>>) {
                    try {
                        if (!response.isSuccessful){
                            callback.onError(RuntimeException(response.message()))
                            return
                        }
                        callback.onSuccess(response.body() ?: throw RuntimeException("body is null"))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
                override fun onFailure(call: Call <List<Post>>, t: Throwable) {
                    callback.onError(Exception(t))
                }
            })
    }

    override fun likeById(id: Long, callback: PostRepository.Callback<Post>) {

        PostsApi.retrofitService.likeById(id)
            .enqueue(object: Callback <Post>{
                override fun onResponse(call: Call <Post>, response: Response <Post>) {
                    try {
                        if (!response.isSuccessful){
                            callback.onError(RuntimeException(response.message()))
                            return
                        }
                        callback.onSuccess( response.body() ?: throw RuntimeException("body is null"))
                    } catch (e: Exception) {
                        callback.onError (e)
                    }
                }
                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(Exception(t))
                }
            })

    }
    override fun unlikeById(id: Long, callback: PostRepository.Callback<Post>) {
        PostsApi.retrofitService.unlikeById(id)
            .enqueue(object: Callback <Post>{
                override fun onResponse(call: Call <Post>, response: Response <Post>) {
                    try {
                        if (!response.isSuccessful){
                            callback.onError(RuntimeException(response.message()))
                            return
                        }
                        callback.onSuccess( response.body() ?: throw RuntimeException("body is null"))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(Exception(t))
                }
            })
    }

    override fun save(post: Post, callback: PostRepository.Callback<Post>) {
        PostsApi.retrofitService.save(post)
            .enqueue(object: Callback <Post>{
                override fun onResponse(call: Call <Post>, response: Response <Post>) {
                    try {
                        if (!response.isSuccessful){
                            callback.onError(RuntimeException(response.message()))
                            return
                        }
                        callback.onSuccess( response.body() ?: throw RuntimeException("body is null"))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(Exception(t))
                }
            })
    }

    override fun removeById(id: Long, callback: PostRepository.Callback<Unit>) {
        PostsApi.retrofitService.removeById(id)
            .enqueue(object: Callback <Unit>{

                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    try {
                        if (!response.isSuccessful){
                            callback.onError(RuntimeException(response.message()))
                            return
                        }
                        callback.onSuccess(Unit)
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    callback.onError(Exception(t))
                }
            })
    }

}
