package com.shubham.hard75.ui.viewmodel


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shubham.hard75.data.db.entities.ChallengeDay
import com.shubham.hard75.data.db.entities.DayStatus
import com.shubham.hard75.data.repositories.ChallengeRepository
import com.shubham.hard75.data.repositories.TaskRepository
import com.shubham.hard75.model.ChallengeUiState
import com.shubham.hard75.model.LeaderboardEntry
import com.shubham.hard75.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit


class ChallengeViewModel(
    private val repository: ChallengeRepository,
    private val taskRepository: TaskRepository, // Injected
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeUiState())
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    // Task list is now a flow from the repository
    val taskList: StateFlow<List<Task>> = taskRepository.taskList

    init {
        val currentUser = auth.currentUser
        _uiState.update { it.copy(userPhotoUrl = currentUser?.photoUrl?.toString()) }

        viewModelScope.launch {
            repository.getAllDays().stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            ).collectLatest { days ->
                if (days.isNotEmpty()) {
                    _uiState.update { it.copy(days = days, isChallengeActive = true) }
                    checkDailyStatus()
                } else {
                    _uiState.update { it.copy(days = emptyList(), isChallengeActive = false) }
                }
            }
        }
    }

    // --- Task Management ---
    fun addTask(taskName: String) {
        taskRepository.addTask(taskName)
    }

    fun deleteTask(task: Task) {
        taskRepository.deleteTask(task)
    }

    // --- Challenge Logic ---
    fun startChallenge() {
        viewModelScope.launch {
            repository.resetChallenge() // Clear any previous attempts
            val today = LocalDate.now()
            val initialDays = (1..75).map { dayNum ->
                ChallengeDay(
                    dayNumber = dayNum,
                    status = if (dayNum == 1) DayStatus.FAILED else DayStatus.LOCKED,
                    totalTasks = taskList.value.size // Use current task list size
                )
            }
            initialDays.forEach { repository.upsertDay(it) }
            _uiState.update { it.copy(challengeStartDate = today, currentDayNumber = 1) }
        }
    }

    fun updateTasksForCurrentDay(completedIds: List<String>) {
        viewModelScope.launch {
            val currentDayNumber = _uiState.value.currentDayNumber
            val dayToUpdate = repository.getDay(currentDayNumber) ?: return@launch

            val totalTasks = taskList.value.size
            val newStatus = when {
                completedIds.isEmpty() -> DayStatus.FAILED
                completedIds.size < totalTasks -> DayStatus.IN_PROGRESS
                else -> DayStatus.COMPLETED
            }

            val newScore = when (newStatus) {
                DayStatus.COMPLETED -> 10
                DayStatus.IN_PROGRESS -> completedIds.size
                else -> 0
            }

            val updatedDay = dayToUpdate.copy(
                status = newStatus,
                score = newScore,
                completedTaskIds = completedIds,
                totalTasks = totalTasks // Update total tasks for this day
            )
            repository.upsertDay(updatedDay)

            if (currentDayNumber == 75 && newStatus == DayStatus.COMPLETED) {
                completeChallenge()
            }
        }
    }

    private fun checkDailyStatus() {
        val startDate = _uiState.value.challengeStartDate ?: return
        val today = LocalDate.now()
        val daysPassed = ChronoUnit.DAYS.between(startDate, today).toInt() + 1

        if (daysPassed > 75) {
            return
        }

        _uiState.update { it.copy(currentDayNumber = daysPassed) }

        viewModelScope.launch {
            val yesterdayNumber = daysPassed - 1
            if (yesterdayNumber > 0) {
                val yesterdayData = repository.getDay(yesterdayNumber)
                if (yesterdayData != null && yesterdayData.status != DayStatus.COMPLETED) {
                    _uiState.update { it.copy(hasFailed = true) }
                    return@launch
                }
            }

            val todayData = repository.getDay(daysPassed)
            if (todayData != null && todayData.status == DayStatus.LOCKED) {
                repository.upsertDay(todayData.copy(status = DayStatus.FAILED))
            }
        }
    }

    private fun completeChallenge() {
        viewModelScope.launch {
            val user = auth.currentUser ?: return@launch
            val allDays = _uiState.value.days
            val totalScore = allDays.sumOf { it.score }

            val entry = LeaderboardEntry(
                userId = user.uid,
                userName = user.displayName ?: "Anonymous",
                totalScore = totalScore
            )

            firestore.collection("leaderboard")
                .document(user.uid)
                .set(entry)
                .addOnSuccessListener { Log.d("ChallengeViewModel", "Leaderboard score saved!") }
                .addOnFailureListener { e -> Log.w("ChallengeViewModel", "Error saving score", e) }
        }
    }

    fun resetChallenge() {
        viewModelScope.launch {
            repository.resetChallenge()
            _uiState.update { ChallengeUiState(userPhotoUrl = _uiState.value.userPhotoUrl) }
        }
    }

    fun dismissFailureDialog() {
        _uiState.update { it.copy(hasFailed = false) }
        resetChallenge()
    }
}
