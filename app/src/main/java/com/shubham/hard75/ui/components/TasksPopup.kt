package com.shubham.hard75.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shubham.hard75.data.db.entities.ChallengeDay
import com.shubham.hard75.model.Task
import com.shubham.hard75.data.db.entities.DayStatus


@Composable
fun TasksPopup(
    tasks: List<Task>,
    dayData: ChallengeDay?,
    onDismiss: () -> Unit,
    onFinish: (List<String>) -> Unit // Callback now returns a list of completed task IDs
) {
    // Keying `remember` to dayData ensures this state re-initializes
    // only when the day's data changes, not on every recomposition.
    // This is the core fix for state persistence within a session.
    val checkedStates = remember(dayData) {
        tasks.map { task ->
            dayData?.completedTaskIds?.contains(task.id) ?: false
        }.toMutableStateList()
    }

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
                            .clickable { checkedStates[index] = !checkedStates[index] }
                            .padding(vertical = 8.dp)
                    ) {
                        Checkbox(
                            checked = checkedStates[index],
                            onCheckedChange = { isChecked -> checkedStates[index] = isChecked }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = task.name)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                // Create a list of IDs for the tasks that are currently checked
                val completedTaskIds = tasks
                    .filterIndexed { index, _ -> checkedStates[index] }
                    .map { it.id }
                onFinish(completedTaskIds)
            }) {
                Text("Finish")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview
@Composable
fun TasksPopupPreview() {
    val sampleTasks = listOf(
        Task("task1", "Read 10 pages"),
        Task("task2", "Workout for 45 minutes"),
        Task("task3", "Drink 1 gallon of water")
    )
    val sampleDayData = ChallengeDay(
        dayNumber = 5,
        status = DayStatus.IN_PROGRESS,
        completedTaskIds = listOf("task2")
    )

    TasksPopup(
        tasks = sampleTasks,
        dayData = sampleDayData,
        onDismiss = {},
        onFinish = {}
    )
}