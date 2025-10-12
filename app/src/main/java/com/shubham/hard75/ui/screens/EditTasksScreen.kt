package com.shubham.hard75.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shubham.hard75.model.Task
import com.shubham.hard75.ui.viewmodel.ChallengeViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun EditTasksScreenRoot(
    onNavigateBack: () -> Unit,
    viewModel: ChallengeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    EditTasksScreen(
        taskList = uiState.taskList.filter { it.id != "selfie" }, // Exclude the static selfie task
        onAddTask = { taskName -> viewModel.addTask(taskName) },
        onDeleteTask = { task -> viewModel.deleteTask(task) },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTasksScreen(
    taskList: List<Task>,
    onAddTask: (String) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onNavigateBack: () -> Unit
) {
    var newTaskName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Tasks") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Input field for adding a new task
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTaskName,
                    onValueChange = { newTaskName = it },
                    label = { Text("New Task Name") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newTaskName.isNotBlank()) {
                            onAddTask(newTaskName)
                            newTaskName = "" // Clear the input field
                        }
                    },
                    enabled = newTaskName.isNotBlank()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List of existing tasks
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(taskList, key = { it.id }) { task ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = task.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onDeleteTask(task) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Task")
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun EditTasksScreenPreview() {
    val sampleTasks = listOf(
        Task(id = "1", name = "Drink 1 gallon of water"),
        Task(id = "2", name = "Follow a diet"),
        Task(id = "3", name = "Read 10 pages of a book"),
        Task(id = "4", name = "45-minute outdoor workout")
    )

    MaterialTheme {
        EditTasksScreen(
            taskList = sampleTasks,
            onAddTask = {},
            onDeleteTask = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditTasksScreenEmptyPreview() {
    MaterialTheme {
        EditTasksScreen(
            taskList = emptyList(),
            onAddTask = {},
            onDeleteTask = {},
            onNavigateBack = {}
        )
    }
}