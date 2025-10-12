package com.shubham.hard75.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.shubham.hard75.data.db.AppDatabase
import com.shubham.hard75.data.repositories.ChallengeRepository
import com.shubham.hard75.data.repositories.TaskRepository
import com.shubham.hard75.ui.viewmodel.AuthViewModel
import com.shubham.hard75.ui.viewmodel.ChallengeViewModel
import com.shubham.hard75.ui.viewmodel.LeaderboardViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single { androidApplication().getSharedPreferences("hard75_prefs", Context.MODE_PRIVATE) }
    single { CredentialManager.create(androidContext()) }

    // Firebase Services
    single { Firebase.auth }
    single { Firebase.firestore }

    // Database & DAO
    single { AppDatabase.getDatabase(androidApplication()) }
    single { get<AppDatabase>().challengeDao() }

    // Utility
    single { Gson() }

    // Repositories
    singleOf(::ChallengeRepository)
    singleOf(::TaskRepository)

    // ViewModel definitions
    viewModelOf(::ChallengeViewModel)
    viewModelOf(::LeaderboardViewModel)
    viewModelOf(::AuthViewModel)
}

