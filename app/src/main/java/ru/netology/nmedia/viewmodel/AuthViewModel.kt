package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ru.netology.nmedia.auth.AppAuth

class AuthViewModel: ViewModel() {
    val state = AppAuth.getInstance().state
        .asLiveData()
    val authenticated: Boolean
    get() = state.value != null
}