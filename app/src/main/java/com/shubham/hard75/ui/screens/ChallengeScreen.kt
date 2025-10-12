package com.shubham.hard75.ui.screens

import android.content.Context
import android.graphics.Bitmap
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.shubham.hard75.data.db.entities.ChallengeDay
import com.shubham.hard75.data.db.entities.DayStatus
import com.shubham.hard75.model.ChallengeUiState
import com.shubham.hard75.model.Task
import com.shubham.hard75.ui.components.CalendarView
import com.shubham.hard75.ui.components.TasksPopup
import com.shubham.hard75.ui.viewmodel.ChallengeViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun ChallengeScreenRoot(
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToEditTasks: () -> Unit,
    onNavigateToGallery: () -> Unit,
    viewModel: ChallengeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ChallengeScreen(
        uiState = uiState,
        onNavigateToLeaderboard = onNavigateToLeaderboard,
        onNavigateToEditTasks = onNavigateToEditTasks,
        onNavigateToGallery = onNavigateToGallery,
        onStartChallenge = viewModel::startChallenge,
        onStartNewAttempt = viewModel::startNewAttempt, // Pass the function here
        updateTasksForCurrentDay = viewModel::updateTasksForCurrentDay,
        updateSelfieForCurrentDay = viewModel::saveSelfieLocally,
        onDismissFailureDialog = viewModel::dismissFailureDialog,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreen(
    uiState: ChallengeUiState,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToEditTasks: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onStartChallenge: () -> Unit,
    onStartNewAttempt: () -> Unit,
    updateTasksForCurrentDay: (List<String>) -> Unit,
    updateSelfieForCurrentDay: (Context, Bitmap, String?) -> Unit,
    onDismissFailureDialog: () -> Unit,
) {
    var showTaskDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showStartFreshDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("75 Hard Challenge") },
                actions = {
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
                    // Dropdown menu for extra options
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Tasks") },
                            onClick = {
                                onNavigateToEditTasks()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Leaderboard") },
                            onClick = {
                                onNavigateToLeaderboard()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Leaderboard,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Gallery") },
                            onClick = {
                                onNavigateToGallery()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.PhotoLibrary,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Start New Attempt") },
                            onClick = {
                                showStartFreshDialog = true
                                showMenu = false
                            }
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
                    enabled = uiState.taskList.isNotEmpty()
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
                        onClick = onStartChallenge,
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
            tasks = uiState.taskList,
            dayData = currentDayData,
            onDismiss = { showTaskDialog = false },
            onFinish = { completedTaskIds ->
                updateTasksForCurrentDay(completedTaskIds)
            },
            onSelfieTaken = { bitmap, note ->
                updateSelfieForCurrentDay(context, bitmap, note)
            }
        )
    }

    if (uiState.hasFailed) {
        AlertDialog(
            onDismissRequest = onDismissFailureDialog,
            title = { Text("Challenge Failed") },
            text = { Text("You missed a day and your streak is broken. You can start a new challenge.") },
            confirmButton = {
                TextButton(onClick = onDismissFailureDialog) {
                    Text("START OVER")
                }
            }
        )
    }

    // Dialog for the "Start Fresh" option
    if (showStartFreshDialog) {
        AlertDialog(
            onDismissRequest = { showStartFreshDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Start a New Attempt?") },
            text = { Text("This will end your current attempt and start a new one on Day 1. Your previous photos and progress will be saved in the gallery.") },
            confirmButton = {
                Button(
                    onClick = {
                        onStartNewAttempt()
                        showStartFreshDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes, Start Fresh")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartFreshDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "Challenge Not Started")
@Composable
fun ChallengeScreenPreview_NotStarted() {
    ChallengeScreen(
        uiState = ChallengeUiState(
            isChallengeActive = false,
            hasFailed = false,
            userPhotoUrl = null,
            taskList = emptyList(),
            days = emptyList()
        ),
        onNavigateToLeaderboard = {},
        onNavigateToEditTasks = {},
        onNavigateToGallery = {},
        onStartChallenge = {},
        onStartNewAttempt = {},
        updateTasksForCurrentDay = {},
        updateSelfieForCurrentDay = { _, _, _ -> },
        onDismissFailureDialog = {}
    )
}

@Preview(showBackground = true, name = "Challenge Active")
@Composable
fun ChallengeScreenPreview_Active() {
    val sampleDays = List(75) { index ->
        ChallengeDay(
            dayNumber = index + 1,
            selfieImageUrl = "https://example.com/photo1.jpg", // Replace with a placeholder URL or drawable
            selfieNote = "Feeling great after the first day! This is going to be a long note to see how it overflows.",
            timestamp = System.currentTimeMillis() - 86400000 * 4,
            status = DayStatus.getRandomStatus(),
            score = 1750,
            totalTasks = 100,
            completedTaskIds = listOf("gym", "water_1l", "walk"),
            attemptNumber = 1
        )
    }
    ChallengeScreen(
        uiState = ChallengeUiState(
            isChallengeActive = true,
            hasFailed = false,
            userPhotoUrl = "https://example.com/user.jpg",
            taskList = listOf(
                Task(id = "1", name = "Drink 1 gallon of water"),
                Task(id = "2", name = "Two 45-minute workouts"),
                Task(id = "3", name = "Follow a diet")
            ),
            days = sampleDays,
            currentDayNumber = 5
        ),
        onNavigateToLeaderboard = {},
        onNavigateToEditTasks = {},
        onNavigateToGallery = {},
        onStartChallenge = {},
        onStartNewAttempt = {},
        updateTasksForCurrentDay = {},
        updateSelfieForCurrentDay = { _,_, _ -> },
        onDismissFailureDialog = {}
    )
}

@Preview(showBackground = true, name = "Challenge Failed")
@Composable
fun ChallengeScreenPreview_Failed() {
    ChallengeScreen(
        uiState = ChallengeUiState(
            isChallengeActive = true,
            hasFailed = true,
            userPhotoUrl = null,
            taskList = emptyList(),
            days = emptyList()
        ),
        onNavigateToLeaderboard = {},
        onNavigateToEditTasks = {},
        onNavigateToGallery = {},
        onStartChallenge = {},
        onStartNewAttempt = {},
        updateTasksForCurrentDay = {},
        updateSelfieForCurrentDay = { _,_, _ -> },
        onDismissFailureDialog = {}
    )
}

