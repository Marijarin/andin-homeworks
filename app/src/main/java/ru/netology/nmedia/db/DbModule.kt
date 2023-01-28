package ru.netology.nmedia.db

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import ru.netology.nmedia.R
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.PostEntity
import java.io.BufferedReader
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DbModule {

    @Singleton
    @Provides
    fun provideDb(
        @ApplicationContext
        context: Context,
        postDao: PostDao
    ): AppDb = Room.databaseBuilder(context, AppDb::class.java, "app.db")
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                CoroutineScope(Dispatchers.IO).launch {
                    fillInDb(context, postDao)
                }
            }
        })
        .fallbackToDestructiveMigration()
        .build()

    private suspend fun fillInDb(
        @ApplicationContext
        context: Context,
        postDao: PostDao
    ) {
        try {
            val posts = loadJSONArray(context)

            for (i in 0 until posts.length()) {
                val item = posts.getJSONObject(i)
                val id = item.getLong("id")
                val author = item.getString("author")
                val authorAvatar = item.getString("authorAvatar")
                val content = item.getString("content")
                val published = item.getString("published")
                val likedByMe = item.getBoolean("likedByMe")
                val attachment = item.getBoolean("attachment")
                val show = item.getBoolean("show")
                val authorId = item.getLong("authorId")
                val postEntity = PostEntity(
                    id = id,
                    author = author,
                    authorAvatar = authorAvatar,
                    content = content,
                    published = published,
                    likedByMe = likedByMe,
                    attachment = null,
                    show = show,
                    authorId = authorId
                )
                postDao.insert(postEntity)
            }
        } catch (e: JSONException) {
            Log.e("***Loading from json***", "fillInDb: $e")
        }
    }


    private fun loadJSONArray(context: Context): JSONArray {
        val inputStream = context.resources.openRawResource(R.raw.posts)
        BufferedReader(inputStream.reader()).use {
            return JSONArray(it.readText())
        }
    }

    @Provides
    fun providePostDao(
        appDb: AppDb
    ): PostDao = appDb.postDao()
}