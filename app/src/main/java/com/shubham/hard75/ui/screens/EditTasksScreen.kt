package com.shubham.hard75.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    viewModel: ChallengeViewModel = koinViewModel(),
) {
    val tasks by viewModel.taskList.collectAsState()

    EditTasksScreen(
        onNavigateBack = onNavigateBack,
        tasks = tasks,
        onAddTask = viewModel::addTask,
        onDeleteTask = viewModel::deleteTask
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTasksScreen(
    onNavigateBack: () -> Unit,
    tasks: List<Task>,
    onAddTask: (String) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    var newTaskName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Your Tasks") },
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
            // List of existing tasks
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(tasks) { task ->
                    TaskItem(
                        task = task,
                        onDelete = { onDeleteTask(task) }
                    )
                }
            }

            // Input for adding a new task
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
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
                            newTaskName = "" // Clear the text field
                        }
                    },
                    enabled = newTaskName.isNotBlank()
                ) {
                    Text("Add")
                }
            }
        }
    }
}

@Composable
private fun TaskItem(task: Task, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = task.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete Task",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun EditTasksScreenPreview() {
    val sampleTasks = listOf(
        Task(id = "1", name = "Workout for 45 minutes"),
        Task(id = "2", name = "Drink 1 gallon of water"),
        Task(id = "3", name = "Read 10 pages of a book")
    )

    EditTasksScreen(
        onNavigateBack = {},
        tasks = sampleTasks,
        onAddTask = {},
        onDeleteTask = {}
    )
}
