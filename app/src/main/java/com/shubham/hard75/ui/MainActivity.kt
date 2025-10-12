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
import com.google.firebase.FirebaseApp
import com.shubham.hard75.ui.screens.ChallengeScreenRoot
import com.shubham.hard75.ui.screens.EditTasksScreen
import com.shubham.hard75.ui.screens.EditTasksScreenRoot
import com.shubham.hard75.ui.screens.LeaderboardScreen
import com.shubham.hard75.ui.screens.LeaderboardScreenRoot
import com.shubham.hard75.ui.screens.LoginScreenRoot
import com.shubham.hard75.ui.theme.Hard75Theme
import org.koin.androidx.compose.koinViewModel

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
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreenRoot(
                authViewModel = koinViewModel(),
                onLoginSuccess = {
                    navController.navigate("challenge") {
                        // Clear the back stack so the user can't go back to the login screen
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("challenge") {
            ChallengeScreenRoot(
                onNavigateToLeaderboard = { navController.navigate("leaderboard") },
                onNavigateToEditTasks = { navController.navigate("edit_tasks") }
            )
        }
        composable("leaderboard") {
            LeaderboardScreenRoot(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("edit_tasks") {
            EditTasksScreenRoot(
                onNavigateBack = { navController.popBackStack() },
                viewModel = koinViewModel() // ChallengeViewModel also manages tasks
            )
        }
    }
}
