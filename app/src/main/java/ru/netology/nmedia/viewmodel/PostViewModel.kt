package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    author = "",
    authorAvatar = "",
    content = "",
    published = "",
    likedByMe = false,
    likes = 0,
    attachment = null,
    saved = false
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl(AppDb.getInstance(application).postDao())
   val data: LiveData<FeedModel>
        get() = repository.data.map {
            FeedModel(it, it.isEmpty())
        }
    private val _dataState = MutableLiveData<FeedModelState>(FeedModelState.Idle)
    val dataState: LiveData<FeedModelState>
    get() = _dataState
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated
    private val _error = SingleLiveEvent<Throwable>()
    val error: LiveData<Throwable>
        get() = _error

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch{
        try {
            _dataState.value = FeedModelState.Loading
            repository.getAll()
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception){
            _dataState.value = FeedModelState.Error
        }
    }
    fun refresh() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState.Refreshing
            repository.checkNotSaved()
            repository.getAll()
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception){
            _error.value = e
            _dataState.value = FeedModelState.Error
        }
    }


    fun save() = viewModelScope.launch{
        edited.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    repository.save(it)
                    _dataState.value = FeedModelState.Idle
                } catch (e: Exception) {
                    _error.value = e
                    _dataState.value = FeedModelState.Error
                }
            }
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }


    fun likeById(id: Long) = viewModelScope.launch{
        try {
            _dataState.value = FeedModelState.Refreshing
            repository.likeById(id)
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _error.value = e
            _dataState.value = FeedModelState.Error
        }
    }

    fun removeById(id: Long) = viewModelScope.launch{
        try {
            _dataState.value = FeedModelState.Refreshing
            repository.removeById(id)
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _error.value = e
            _dataState.value = FeedModelState.Error
        }
    }

}
