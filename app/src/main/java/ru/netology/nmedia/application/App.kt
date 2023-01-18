package ru.netology.nmedia.application

import android.app.Application
import ru.netology.nmedia.repository.di.DependencyContainer


class App: Application() {
    override fun onCreate() {
        super.onCreate()
        DependencyContainer.initApp(this)
    }
}