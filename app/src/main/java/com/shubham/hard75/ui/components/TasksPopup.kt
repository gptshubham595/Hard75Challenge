package com.shubham.hard75.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.shubham.hard75.data.db.entities.ChallengeDay
import com.shubham.hard75.data.db.entities.DayStatus
import com.shubham.hard75.model.Task


@Composable
fun TasksPopup(
    tasks: List<Task>,
    dayData: ChallengeDay?,
    onDismiss: () -> Unit,
    onFinish: (List<String>) -> Unit,
    onSelfieTaken: (Bitmap, String?) -> Unit
) {
    var showNoteDialog by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current
    val checkedStates = remember(dayData) {
        tasks.map { task ->
            dayData?.completedTaskIds?.contains(task.id) ?: false
        }.toMutableStateList()
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            showNoteDialog = bitmap
        }
    }

    // New: Launcher for requesting the camera permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, launch the camera
            cameraLauncher.launch()
        } else {
            // Handle permission denial if needed (e.g., show a message)
        }
    }

    // --- Main Task List Dialog ---
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Day ${dayData?.dayNumber ?: "-"} Tasks") },
        text = {
            LazyColumn {
                itemsIndexed(tasks) { index, task ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (task.id != "selfie") {
                                    checkedStates[index] = !checkedStates[index]
                                }
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checkedStates[index],
                                onCheckedChange = { isChecked ->
                                    if (task.id != "selfie") {
                                        checkedStates[index] = isChecked
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = task.name)
                        }
                        if (task.id == "selfie") {
                            IconButton(onClick = {
                                // Check for permission before launching camera
                                when (PackageManager.PERMISSION_GRANTED) {
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) -> {
                                        // Permission is already granted
                                        cameraLauncher.launch()
                                    }

                                    else -> {
                                        // Permission has not been granted, request it
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            }) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Take Selfie")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                // 1. Get the list of tasks the user manually checked/unchecked.
                val manuallyCheckedIds = tasks.filterIndexed { index, task ->
                    task.id != "selfie" && checkedStates[index]
                }.map { it.id }

                // 2. Check if the selfie task was already completed.
                // If it was, we must add it back to the list to preserve its state.
                val finalCompletedIds = if (dayData?.completedTaskIds?.contains("selfie") == true) {
                    (manuallyCheckedIds + "selfie").distinct()
                } else {
                    manuallyCheckedIds
                }

                // 3. Pass the correct and complete list to the ViewModel.
                onFinish(finalCompletedIds)
            }) { Text("Update Tasks") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )

    // --- Note Dialog (appears after photo is taken) ---
    if (showNoteDialog != null) {
        var noteText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNoteDialog = null },
            title = { Text("Add a Note (Optional)") },
            text = {
                TextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("How was your day?") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    onSelfieTaken(showNoteDialog!!, noteText.takeIf { it.isNotBlank() })
                    showNoteDialog = null
                }) { Text("Save Selfie") }
            }
        )
    }
}

@Preview
@Composable
fun TasksPopupPreview() {
    val sampleTasks = listOf(
        Task(id = "gym", name = "Go to the Gym"),
        Task(id = "water_1l", name = "Drink 1L Water"),
        Task(id = "water_2l", name = "Drink 2L Water"),
        Task(id = "water_3l", name = "Drink 3L Water"),
        Task(id = "walk", name = "Outdoor Walk"),
        Task(id = "read", name = "Read 10 pages"),
        Task(id = "steps_5k", name = "Complete 5k steps"),
        Task(id = "steps_10k", name = "Complete 10k steps"),
        Task(id = "no_junk", name = "No Junk Food")
    )
    val sampleDayData = ChallengeDay(
        dayNumber = 5,
        status = DayStatus.IN_PROGRESS,
        completedTaskIds = listOf("task2"),
        score = 100,
        totalTasks = 9,
        selfieImageUrl = "",
        selfieNote = "",
        timestamp = System.currentTimeMillis(),
        attemptNumber = 1
    )

    TasksPopup(
        tasks = sampleTasks,
        dayData = sampleDayData,
        onDismiss = {},
        onFinish = {},
        onSelfieTaken = { _, _ -> }
    )
}