package com.shubham.hard75.data.repositories

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shubham.hard75.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

private const val TASKS_KEY = "user_tasks_list"

class TaskRepository(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) {
    private val defaultTasks = listOf(
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

    private val _taskList = MutableStateFlow<List<Task>>(emptyList())
    val taskList: StateFlow<List<Task>> = _taskList.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        val tasksJson = sharedPreferences.getString(TASKS_KEY, null)
        if (tasksJson != null) {
            val type = object : TypeToken<List<Task>>() {}.type
            _taskList.update {
                gson.fromJson(tasksJson, type)
            }
        } else {
            // First launch, save default tasks
            _taskList.update { defaultTasks }
            saveTasks()
        }
    }

    fun addTask(taskName: String) {
        val newTask = Task(id = UUID.randomUUID().toString(), name = taskName)
        _taskList.value = _taskList.value + newTask
        saveTasks()
    }

    fun deleteTask(task: Task) {
        _taskList.value = _taskList.value.filterNot { it.id == task.id }
        saveTasks()
    }

    private fun saveTasks() {
        val tasksJson = gson.toJson(_taskList.value)
        sharedPreferences.edit {
            putString(TASKS_KEY, tasksJson)
            apply()
        }
    }
}
