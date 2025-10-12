package com.shubham.hard75.ui.viewmodel


import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID


class ChallengeViewModel(
    private val challengeRepository: ChallengeRepository,
    private val taskRepository: TaskRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeUiState())
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    init {
        val currentUser = auth.currentUser
        _uiState.update { it.copy(userPhotoUrl = currentUser?.photoUrl?.toString()) }

        viewModelScope.launch {
            combine(
                challengeRepository.getDaysForCurrentAttempt(),
                taskRepository.getAllTasks()
            ) { days, tasks ->
                val fullTaskList = tasks.toMutableList().apply {
                    if (none { it.id == "selfie" }) {
                        add(0, Task(id = "selfie", name = "Attach today's selfie"))
                    }
                }
                ChallengeUiState(
                    days = days,
                    taskList = fullTaskList,
                    isChallengeActive = days.isNotEmpty(),
                    userPhotoUrl = _uiState.value.userPhotoUrl
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ChallengeUiState()
            ).collectLatest { state ->
                _uiState.value = state
                if (state.isChallengeActive) {
                    checkDailyStatus()
                }
            }
        }
    }

    // --- Task Management ---
    fun addTask(taskName: String) {
        viewModelScope.launch { taskRepository.addTask(taskName) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { taskRepository.deleteTask(task) }
    }

    // --- Selfie Management ---
    fun saveSelfieLocally(context: Context, bitmap: Bitmap, note: String?) {
        viewModelScope.launch {
            val currentDay = _uiState.value.currentDayNumber
            val dayData = challengeRepository.getDay(currentDay) ?: return@launch
            val filename = "day_${currentDay}_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, filename)

            try {
                FileOutputStream(file).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
                }
                val localUri = Uri.fromFile(file).toString()
                val updatedDay = dayData.copy(
                    selfieImageUrl = localUri,
                    selfieNote = note,
                    timestamp = System.currentTimeMillis()
                )
                challengeRepository.upsertDay(updatedDay)
                val updatedTaskIds = dayData.completedTaskIds.toMutableList().apply {
                    if (!contains("selfie")) add("selfie")
                }
                updateTasksForCurrentDay(updatedTaskIds)
            } catch (e: Exception) {
                Log.e("ChallengeViewModel", "Failed to save selfie locally", e)
            }
        }
    }

    // --- Challenge & Attempt Logic ---
    fun startChallenge() {
        startNewAttempt(isFirstEverAttempt = true)
    }

    fun startNewAttempt(isFirstEverAttempt: Boolean = false) {
        viewModelScope.launch {
            if (!isFirstEverAttempt) {
                challengeRepository.startNewAttempt()
            }
            val newAttemptNumber = challengeRepository.getCurrentAttemptNumber()
            val initialDays = (1..75).map { dayNum ->
                ChallengeDay(
                    attemptNumber = newAttemptNumber,
                    dayNumber = dayNum,
                    status = if (dayNum == 1) DayStatus.FAILED else DayStatus.LOCKED,
                    totalTasks = _uiState.value.taskList.size
                )
            }
            initialDays.forEach { challengeRepository.upsertDay(it) }
        }
    }

    fun updateTasksForCurrentDay(completedIds: List<String>) {
        viewModelScope.launch {
            val currentDayNumber = _uiState.value.currentDayNumber
            val dayToUpdate = challengeRepository.getDay(currentDayNumber) ?: return@launch
            val totalTasks = _uiState.value.taskList.size
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
                totalTasks = totalTasks
            )
            challengeRepository.upsertDay(updatedDay)
            if (currentDayNumber == 75 && newStatus == DayStatus.COMPLETED) {
                completeChallenge()
            }
        }
    }

    private fun checkDailyStatus() {
        val startDate = challengeRepository.getStartDateForCurrentAttempt() ?: return
        val today = LocalDate.now()
        val daysPassed = ChronoUnit.DAYS.between(startDate, today).toInt() + 1
        if (daysPassed > 75 || daysPassed < 1) return

        if (_uiState.value.currentDayNumber != daysPassed) {
            _uiState.update { it.copy(currentDayNumber = daysPassed) }
        }

        viewModelScope.launch {
            val yesterdayData = challengeRepository.getDay(daysPassed - 1)
            if (yesterdayData != null && yesterdayData.status != DayStatus.COMPLETED) {
                _uiState.update { it.copy(hasFailed = true) }
                return@launch
            }
            val todayData = challengeRepository.getDay(daysPassed)
            if (todayData != null && todayData.status == DayStatus.LOCKED) {
                challengeRepository.upsertDay(todayData.copy(status = DayStatus.FAILED))
            }
        }
    }

    private fun completeChallenge() {
        viewModelScope.launch {
            val user = auth.currentUser ?: return@launch
            val totalScore = _uiState.value.days.sumOf { it.score }
            val entry = LeaderboardEntry(
                userId = user.uid,
                userName = user.displayName ?: "Anonymous",
                totalScore = totalScore
            )
            firestore.collection("leaderboard").document(user.uid).set(entry)
                .addOnFailureListener { e -> Log.w("ChallengeViewModel", "Error saving score", e) }
        }
    }

    fun dismissFailureDialog() {
        _uiState.update { it.copy(hasFailed = false) }
        startNewAttempt()
    }
}

