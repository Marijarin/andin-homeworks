package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

private val empty = Post(
    id = 0,
    author = "",
    authorAvatar = "",
    content = "",
    published = "",
    likedByMe = false,
    likes = 0,
    attachment = null,
    saved = false,
    authorId = 0
)
private val noPhoto = PhotoModel(null, null)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {

   val data: LiveData<FeedModel> = appAuth.state.map {it?.id}.flatMapLatest { id ->
            repository.data
                .map { posts ->
            FeedModel(posts.map{it.copy(ownedByMe = it.authorId==id)}, posts.isEmpty())}
        }.asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>(FeedModelState.Idle)
    val dataState: LiveData<FeedModelState>
    get() = _dataState

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
    get() = _photo

   val newerCount: LiveData<Int> = data.switchMap {
        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)
            .catch { e -> e.printStackTrace() }
            .asLiveData(Dispatchers.Default)
    }

    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated


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
            _dataState.value = FeedModelState.Error
        }
    }


    fun save() = viewModelScope.launch{
        edited.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    when(_photo.value){
                        noPhoto ->  repository.save(it)
                        else -> _photo.value?.file?.let{ file ->
                            repository.saveWithAttachment(it, file)
                        }
                    }
                    _dataState.value = FeedModelState.Idle
                } catch (e: Exception) {
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
            _dataState.value = FeedModelState.Error
        }
    }
    fun unlikeById(id: Long) = viewModelScope.launch{
        try {
            _dataState.value = FeedModelState.Refreshing
            repository.unlikeById(id)
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun removeById(id: Long) = viewModelScope.launch{
        try {
            _dataState.value = FeedModelState.Refreshing
            repository.removeById(id)
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }
    fun update()= viewModelScope.launch {
        try {
            _dataState.value = FeedModelState.Refreshing
            repository.update()
            _dataState.value = FeedModelState.Idle
        }catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }




}
