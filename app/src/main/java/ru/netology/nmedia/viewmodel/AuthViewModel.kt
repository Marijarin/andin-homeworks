package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.model.PhotoModel
import java.io.File
import javax.inject.Inject
@HiltViewModel
class AuthViewModel @Inject constructor(
     private val appAuth: AppAuth
) : ViewModel() {
    val data: LiveData<AuthState> = appAuth.state
        .asLiveData(Dispatchers.Default)
    val authenticated: Boolean
        get() = appAuth.state.value.id != 0L

    private val noPhoto = PhotoModel(null, null)

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    fun updateUser(login: String, password: String) = viewModelScope.launch {
        try {
            appAuth.updateUser(login, password)
        } catch (e: Exception) {
            println(e)
            throw AppError.from(e)
        }
    }

    fun registerUser(login: String, password: String, name: String) = viewModelScope.launch {
        try {
            appAuth.registerUser(login, password, name)
        } catch (e: Exception) {
            println(e)
            throw AppError.from(e)
        }
    }

    fun registerWithPhoto(login: String, password: String, name: String, file: File) = viewModelScope.launch {
        try {
            appAuth.registerWithPhoto(login, password, name, file)
        } catch (e: Exception) {
            println(e)
            throw AppError.from(e)
        }
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

}