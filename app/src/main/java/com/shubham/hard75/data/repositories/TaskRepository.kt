package com.shubham.hard75.data.repositories

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shubham.hard75.model.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    init {
        // Load initial tasks from storage
        loadTasks()
    }

    /**
     * Exposes the current list of tasks as a Flow that the UI can observe.
     */
    fun getAllTasks(): Flow<List<Task>> = _taskList.asStateFlow()

    /**
     * Adds a new task to the user's list and saves it.
     */
    suspend fun addTask(taskName: String) {
        val newTask = Task(id = UUID.randomUUID().toString(), name = taskName)
        _taskList.update { it + newTask }
        saveTasks()
    }

    /**
     * Deletes a task from the user's list and saves the changes.
     */
    suspend fun deleteTask(task: Task) {
        _taskList.update { it.filterNot { it.id == task.id } }
        saveTasks()
    }

    /**
     * Loads the task list from SharedPreferences. If no tasks are saved,
     * it initializes the list with default tasks.
     */
    private fun loadTasks() {
        val tasksJson = sharedPreferences.getString(TASKS_KEY, null)
        if (tasksJson != null) {
            val type = object : TypeToken<List<Task>>() {}.type
            _taskList.update { gson.fromJson(tasksJson, type) }
        } else {
            // This is the first launch, so load and save the default tasks
            _taskList.update { defaultTasks }
            // Launch a coroutine to save without blocking the init block
            CoroutineScope(Dispatchers.IO).launch { saveTasks() }
        }
    }

    /**
     * Saves the current task list to SharedPreferences as a JSON string.
     * This is a suspend function to ensure file I/O is done off the main thread.
     */
    private suspend fun saveTasks() = withContext(Dispatchers.IO) {
        val tasksJson = gson.toJson(_taskList.value)
        sharedPreferences.edit {
            putString(TASKS_KEY, tasksJson)
        }
    }

    companion object {
        private const val TASKS_KEY = "user_tasks_list"
    }
}

