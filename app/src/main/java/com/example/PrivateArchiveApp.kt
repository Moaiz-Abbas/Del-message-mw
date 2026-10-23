package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.MessageRepository
import com.example.data.PreferencesManager

class PrivateArchiveApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: MessageRepository
        private set

    lateinit var preferencesManager: PreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        repository = MessageRepository(this, database.messageDao(), database.mediaDao())
        preferencesManager = PreferencesManager(this)
    }

    companion object {
        lateinit var instance: PrivateArchiveApp
            private set
    }
}
