package com.shubham.hard75.model
data class LeaderboardState(
    val entries: List<LeaderboardEntry> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
