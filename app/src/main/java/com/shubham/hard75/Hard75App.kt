package com.shubham.hard75

import android.app.Application
import com.google.firebase.FirebaseApp
import com.shubham.hard75.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class Hard75App : Application() {
    override fun onCreate() {
        FirebaseApp.initializeApp(this)
        super.onCreate()
        startKoin {
            // Log Koin activity
            androidLogger()
            // Declare Android context
            androidContext(this@Hard75App)
            // Declare modules to use
            modules(appModule)
        }
    }
}