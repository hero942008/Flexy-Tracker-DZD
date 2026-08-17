package com.example

import android.app.Application
import com.example.data.local.FlexyDatabase
import com.example.data.repository.FlexyRepository

class FlexyApp : Application() {

    val database by lazy { FlexyDatabase.getDatabase(this) }
    val repository by lazy { FlexyRepository(database.flexyDao(), this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: FlexyApp
            private set
    }
}
