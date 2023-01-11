package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.model.PhotoModel
import java.io.File

class AuthViewModel : ViewModel() {
    val state = AppAuth.getInstance().state
        .asLiveData()
    val authenticated: Boolean
        get() = state.value != null

    private val noPhoto = PhotoModel(null, null)

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    fun updateUser(login: String, password: String) = viewModelScope.launch {
        try {
            AppAuth.getInstance().updateUser(login, password)
        } catch (e: Exception) {
            println(e)
            throw AppError.from(e)
        }
    }

    fun registerUser(login: String, password: String, name: String) = viewModelScope.launch {
        try {
            AppAuth.getInstance().registerUser(login, password, name)
        } catch (e: Exception) {
            println(e)
            throw AppError.from(e)
        }
    }

    fun registerWithPhoto(login: String, password: String, name: String, file: File) = viewModelScope.launch {
        try {
            AppAuth.getInstance().registerWithPhoto(login, password, name, file)
        } catch (e: Exception) {
            println(e)
            throw AppError.from(e)
        }
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

}