package com.shubham.hard75.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shubham.hard75.model.LeaderboardEntry
import com.shubham.hard75.model.LeaderboardState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LeaderboardViewModel(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _leaderboardState = MutableStateFlow(LeaderboardState())
    val leaderboardState = _leaderboardState.asStateFlow()

    init {
        fetchLeaderboard()
    }

    private fun fetchLeaderboard() {
        viewModelScope.launch {
            _leaderboardState.update { it.copy(isLoading = true) }

            firestore.collection("leaderboard")
                .orderBy("score", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _leaderboardState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to load leaderboard: ${error.message}"
                            )
                        }
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val entries = snapshot.toObjects(LeaderboardEntry::class.java)
                        _leaderboardState.update {
                            it.copy(
                                isLoading = false,
                                entries = entries,
                                error = null
                            )
                        }
                    }
                }
        }
    }
}