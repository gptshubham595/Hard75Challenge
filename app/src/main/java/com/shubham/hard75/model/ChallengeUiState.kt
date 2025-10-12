package com.shubham.hard75.model

import com.shubham.hard75.data.db.entities.ChallengeDay
import java.time.LocalDate

data class ChallengeUiState(
    val days: List<ChallengeDay> = emptyList(),
    val taskList: List<Task> = emptyList(),
    val currentDayNumber: Int = 1,
    val isChallengeActive: Boolean = false,
    val hasFailed: Boolean = false,
    val userPhotoUrl: String? = null
)
