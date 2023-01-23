package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context) {

    private val TOKEN_KEY = "TOKEN_KEY"
    private val ID_KEY = "ID_KEY"
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _state: MutableStateFlow<AuthState>
    init {
        val id = prefs.getLong(ID_KEY, 0)
        val token = prefs.getString(TOKEN_KEY, null)

        if (id == 0L || token == null) {
            _state = MutableStateFlow(AuthState())
            with(prefs.edit()) {
                clear()
                apply()
            }
        } else {
            _state = MutableStateFlow(AuthState(id, token))
        }
        sendPushToken()
    }

    val state = _state.asStateFlow()

    @Synchronized
    fun setAuth(id: Long, token: String) {
        prefs.edit {
            putLong(ID_KEY, id)
            putString(TOKEN_KEY, token)
        }
        _state.value = AuthState(id, token)
        sendPushToken()
    }

    @Synchronized
    fun removeAuth() {
        prefs.edit {
            clear()
        }
        _state.value = AuthState()
        sendPushToken()
    }
    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint{
        fun getApiService(): ApiService
    }

    suspend fun updateUser(login: String, password: String) {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
            val response = entryPoint.getApiService().updateUser(login, password)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val newAuth = response.body() ?: throw ApiError(response.code(), response.message())
            newAuth.token?.let { setAuth(newAuth.id, it) }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw  UnknownError
        }
    }

    suspend fun registerUser(login: String, password: String, name: String) {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
            val response =
                entryPoint.getApiService().registerUser(login, password, name)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val newAuth = response.body() ?: throw ApiError(response.code(), response.message())
            newAuth.token?.let { setAuth(newAuth.id, it) }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw  UnknownError
        }
    }

    suspend fun registerWithPhoto(login: String, password: String, name: String, file: File) {
        try {
            val fileData = MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody()
            )
            val entryPoint = EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
            val response = entryPoint.getApiService().registerWithPhoto(
                login.toRequestBody(),
                password.toRequestBody(),
                name.toRequestBody(),
                fileData
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val newAuth = response.body() ?: throw ApiError(response.code(), response.message())
            newAuth.token?.let { setAuth(newAuth.id, it) }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw  UnknownError
        }
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
                entryPoint.getApiService().sendPushToken(
                    PushToken(
                        token ?: FirebaseMessaging.getInstance().token.await()
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}