package com.shubham.hard75.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.shubham.hard75.ui.screens.ChallengeScreenRoot
import com.shubham.hard75.ui.screens.EditTasksScreenRoot
import com.shubham.hard75.ui.screens.GalleryScreenRoot
import com.shubham.hard75.ui.screens.LeaderboardScreenRoot
import com.shubham.hard75.ui.screens.LoginScreenRoot
import com.shubham.hard75.ui.theme.Hard75Theme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Hard75Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Key Fix: Check if a user is already signed in.
    val startDestination = if (Firebase.auth.currentUser != null) {
        "challenge" // If user exists, go straight to the main screen
    } else {
        "login" // Otherwise, show the login screen
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreenRoot(
                onLoginSuccess = {
                    // After successful login, navigate to challenge and clear the back stack
                    navController.navigate("challenge") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("challenge") {
            ChallengeScreenRoot(
                onNavigateToLeaderboard = { navController.navigate("leaderboard") },
                onNavigateToEditTasks = { navController.navigate("edit_tasks") },
                onNavigateToGallery = { navController.navigate("gallery") }
            )
        }
        composable("leaderboard") {
            LeaderboardScreenRoot(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("edit_tasks") {
            EditTasksScreenRoot(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("gallery") {
            GalleryScreenRoot(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
