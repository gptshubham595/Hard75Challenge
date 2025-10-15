package com.shubham.hard75.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shubham.hard75.data.db.entities.ChallengeDay
import com.shubham.hard75.data.db.entities.DayStatus
import com.shubham.hard75.data.db.entities.DayStatus.Companion.stillHasHope
import com.shubham.hard75.data.repositories.ChallengeRepository
import com.shubham.hard75.data.repositories.TaskRepository
import com.shubham.hard75.model.ChallengeUiState
import com.shubham.hard75.model.LeaderboardEntry
import com.shubham.hard75.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class ChallengeViewModel(
    private val challengeRepository: ChallengeRepository,
    private val taskRepository: TaskRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val application: Application
) : ViewModel(), DefaultLifecycleObserver {

    private val _uiState = MutableStateFlow(ChallengeUiState())
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

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

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        if (_uiState.value.isChallengeActive) {
            checkDailyStatus()
        }
    }

    fun isWithinGracePeriod(timestamp: Long): Boolean {
        // 2-hour grace period in milliseconds
        val twoHoursInMillis = 2 * 60 * 60 * 1000
        val gracePeriodEnd = timestamp + twoHoursInMillis
        return System.currentTimeMillis() < gracePeriodEnd
    }

    fun LocalDate.isSameDay(lastDayTimeStamp: Long): Boolean {
        // Convert the timestamp to a calendar date in the user's timezone
        val lastDayDate = Instant.ofEpochMilli(lastDayTimeStamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return this.isEqual(lastDayDate)
                || (this.isEqual(lastDayDate.plusDays(1))
                && isWithinGracePeriod(lastDayTimeStamp))
    }

    fun LocalDate.isNextDay(lastDayTimeStamp: Long): Boolean {
        val lastDayDate = Instant.ofEpochMilli(lastDayTimeStamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return this.isEqual(lastDayDate.plusDays(1)) && !isWithinGracePeriod(lastDayTimeStamp)
    }

    fun LocalDate.isMoreThanOneDay(lastDayTimeStamp: Long): Boolean {
        val lastDayDate = Instant.ofEpochMilli(lastDayTimeStamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return this.isAfter(lastDayDate.plusDays(1))
    }

    private fun checkDailyStatus() {
        viewModelScope.launch {
            val latestDay = challengeRepository.getLatestUpdatedDay() ?: return@launch
            val lastTimestamp = latestDay.timestamp ?: return@launch


            val today = LocalDate.now()

            // Case 1: Still the same calendar day.
            if (today.isSameDay(lastTimestamp)) {
                _uiState.update { it.copy(currentDayNumber = latestDay.dayNumber) }
                return@launch
            }

            // Case 2: It's the very next day.
            if (today.isNextDay(lastTimestamp)) {
                if (latestDay.status.stillHasHope()) {
                    // Success! Unlock the next day.
                    val nextDayNumber = latestDay.dayNumber + 1
                    if (nextDayNumber > 75) return@launch // Challenge finished

                    val nextDay = challengeRepository.getDay(nextDayNumber)
                    if (nextDay != null && nextDay.status == DayStatus.LOCKED) {
                        val unlockedNextDay = nextDay.copy(
                            status = DayStatus.FAILED, // Start as red
                            timestamp = System.currentTimeMillis() // Set today's timestamp
                        )
                        challengeRepository.upsertDay(unlockedNextDay)
                        _uiState.update { it.copy(currentDayNumber = nextDayNumber) }
                    }
                } else {
                    // Failed to complete yesterday's tasks.
                    challengeRepository.upsertDay(latestDay.copy(status = DayStatus.FAILED))
                    _uiState.update { it.copy(hasFailed = true) }
                }
                return@launch
            }

            // Case 3: More than one day has passed. You missed a day.
            if (today.isMoreThanOneDay(lastTimestamp)) {
                // Mark the last day as failed for clarity in the calendar
                if (latestDay.status.stillHasHope()) {
                    challengeRepository.upsertDay(latestDay.copy(status = DayStatus.FAILED))
                }
                _uiState.update { it.copy(hasFailed = true) }
            }
        }
    }

    // --- Challenge & Attempt Logic (No changes needed here) ---

    fun startChallenge() {
        viewModelScope.launch {
            initializeDaysForNewAttempt()
        }
    }

    fun startNewAttempt() {
        viewModelScope.launch {
            challengeRepository.startNewAttempt()
            initializeDaysForNewAttempt()
        }
    }

    private suspend fun initializeDaysForNewAttempt() {
        val newAttemptNumber = challengeRepository.getCurrentAttemptNumber()
        val totalTasks = taskRepository.getAllTasks().first().size
        val day1 = ChallengeDay(
            attemptNumber = newAttemptNumber,
            dayNumber = 1,
            status = DayStatus.FAILED,
            totalTasks = totalTasks,
            timestamp = System.currentTimeMillis()
        )
        challengeRepository.upsertDay(day1)

        val futureDays = (2..75).map { dayNum ->
            ChallengeDay(
                attemptNumber = newAttemptNumber,
                dayNumber = dayNum,
                status = DayStatus.LOCKED,
                totalTasks = totalTasks,
                timestamp = null
            )
        }
        futureDays.forEach { challengeRepository.upsertDay(it) }
    }

    // --- Task and Selfie Updates (No changes needed here) ---

    fun updateTasksForCurrentDay(completedIds: List<String>) {
        viewModelScope.launch {
            val currentDayNumber = _uiState.value.currentDayNumber
            val dayToUpdate = challengeRepository.getDay(currentDayNumber) ?: return@launch
            val totalTasks = _uiState.value.taskList.size
            val newStatus = when {
                completedIds.size == totalTasks -> DayStatus.COMPLETED
                completedIds.isNotEmpty() -> DayStatus.IN_PROGRESS
                else -> DayStatus.FAILED
            }
            val newScore = when (newStatus) {
                DayStatus.COMPLETED -> 10
                DayStatus.IN_PROGRESS -> completedIds.count { it != "selfie" }
                else -> 0
            }
            val updatedDay = dayToUpdate.copy(
                status = newStatus,
                score = newScore,
                completedTaskIds = completedIds,
                totalTasks = totalTasks,
                timestamp = System.currentTimeMillis()
            )
            challengeRepository.upsertDay(updatedDay)

            if (currentDayNumber == 75 && newStatus == DayStatus.COMPLETED) {
                completeChallenge()
            }
        }
    }

    fun saveSelfieLocally(bitmap: Bitmap, note: String?) {
        viewModelScope.launch {
            val currentDay = _uiState.value.currentDayNumber
            val dayData = challengeRepository.getDay(currentDay) ?: return@launch
            val filename = "day_${currentDay}_${UUID.randomUUID()}.jpg"
            val file = File(application.filesDir, filename)

            try {
                FileOutputStream(file).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
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

    override fun onCleared() {
        super.onCleared()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }
}