package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.error.AppError

class AuthViewModel: ViewModel() {
    val state = AppAuth.getInstance().state
        .asLiveData()
    val authenticated: Boolean
    get() = state.value != null

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

}