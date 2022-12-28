package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppAuth private constructor(context: Context) {


    companion object {
        const val TOKEN_KEY = "TOKEN_KEY"
        const val ID_KEY = "ID_KEY"

        private var INSTANCE: AppAuth? = null

        fun getInstance(): AppAuth = requireNotNull(INSTANCE){
            "init() must be called before getInstance()"
        }

        fun init(context: Context){
            INSTANCE = AppAuth(context)
        }
    }

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _state: MutableStateFlow<AuthState?>

    init {
        val token = prefs.getString(TOKEN_KEY, null)
        val id = prefs.getLong(ID_KEY, 0L)

        if (token == null || !prefs.contains(ID_KEY)) {
            prefs.edit {
                clear()
            }
            _state = MutableStateFlow(null)
        } else {
            _state = MutableStateFlow(AuthState(id, token))
        }
    }

    val state = _state.asStateFlow()

    @Synchronized
    fun setAuth(id: Long, token: String) {
        prefs.edit {
            putLong(ID_KEY, id)
            putString(TOKEN_KEY, token)
        }
        _state.value = AuthState(id, token)
    }

    @Synchronized
    fun removeAuth(){
        prefs.edit{
            clear()
        }
        _state.value = null
    }
}