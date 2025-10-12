package com.shubham.hard75.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.shubham.hard75.model.ChallengeUiState
import com.shubham.hard75.model.Task
import com.shubham.hard75.ui.components.CalendarView
import com.shubham.hard75.ui.components.TasksPopup
import com.shubham.hard75.ui.viewmodel.ChallengeViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun ChallengeScreenRoot(
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToEditTasks: () -> Unit, // New callback for navigation
    viewModel: ChallengeViewModel = koinViewModel()
) {
    val uiState: ChallengeUiState by viewModel.uiState.collectAsState()
    val taskList: List<Task> by viewModel.taskList.collectAsState() // Collect task list state

    ChallengeScreen(
        onNavigateToLeaderboard = onNavigateToLeaderboard,
        onNavigateToEditTasks = onNavigateToEditTasks, // Pass callback
        startChallenge = viewModel::startChallenge,
        updateTasksForCurrentDay = viewModel::updateTasksForCurrentDay,
        taskList = taskList, // Pass state-driven task list
        dismissFailureDialog = viewModel::dismissFailureDialog,
        uiState = uiState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreen(
    onNavigateToLeaderboard: (() -> Unit)?,
    onNavigateToEditTasks: (() -> Unit)?, // New callback parameter
    uiState: ChallengeUiState,
    startChallenge: (() -> Unit)?,
    taskList: List<Task>,
    updateTasksForCurrentDay: ((List<String>) -> Unit)?,
    dismissFailureDialog: (() -> Unit)?,
) {
    var showTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("75 Hard Challenge") },
                actions = {
                    if (onNavigateToEditTasks != null) {
                        IconButton(onClick = onNavigateToEditTasks) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Tasks")
                        }
                    }

                    if (onNavigateToLeaderboard != null) {
                        IconButton(onClick = onNavigateToLeaderboard) {
                            Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard")
                        }
                    }

                    // User Profile Icon
                    if (!uiState.userPhotoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = uiState.userPhotoUrl,
                            contentDescription = "User Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "User Profile",
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(36.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.isChallengeActive) {
                Button(
                    onClick = { showTaskDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = taskList.isNotEmpty() // Disable button if there are no tasks
                ) {
                    Text("FINISH TODAY'S TASK")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isChallengeActive) {
                CalendarView(days = uiState.days)
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Ready to start the challenge?")
                    Button(
                        onClick = { startChallenge?.invoke() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("START DAY 1")
                    }
                }
            }
        }
    }

    if (showTaskDialog) {
        val currentDayData = uiState.days.find { it.dayNumber == uiState.currentDayNumber }
        TasksPopup(
            tasks = taskList,
            dayData = currentDayData,
            onDismiss = { showTaskDialog = false },
            onFinish = { completedTaskIds: List<String> ->
                updateTasksForCurrentDay?.invoke(completedTaskIds)
                showTaskDialog = false
            }
        )
    }

    if (uiState.hasFailed) {
        AlertDialog(
            onDismissRequest = { dismissFailureDialog?.invoke() },
            title = { Text("Challenge Failed") },
            text = { Text("You missed a day and your streak is broken. You can start a new challenge.") },
            confirmButton = {
                TextButton(onClick = { dismissFailureDialog?.invoke() }) {
                    Text("START OVER")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChallengeScreenPreview() {
    val taskList = listOf(
        Task(id = "gym", name = "Go to the Gym"),
        Task(id = "water_1l", name = "Drink 1L Water"),
        Task(id = "walk", name = "Outdoor Walk"),
        Task(id = "read", name = "Read 10 pages"),
    )

    ChallengeScreen(
        onNavigateToLeaderboard = {},
        onNavigateToEditTasks = {},
        uiState = ChallengeUiState(userPhotoUrl = ""), // Example photo URL for preview
        startChallenge = {},
        taskList = taskList,
        updateTasksForCurrentDay = {},
        dismissFailureDialog = {}
    )
}