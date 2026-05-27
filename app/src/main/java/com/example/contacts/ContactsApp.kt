package com.example.contacts

import android.app.Application

class ContactsApp : Application() {
    lateinit var syncManager: SyncManager
        private set

    override fun onCreate() {
        super.onCreate()
        val dao = AppDatabase.get(this).contactDao()
        syncManager = SyncManager(this, dao)
        syncManager.start()
    }
}