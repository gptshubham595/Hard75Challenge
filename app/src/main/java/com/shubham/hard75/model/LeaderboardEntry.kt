package com.shubham.hard75.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Represents a single entry in the leaderboard.
 * This data class is used for serialization/deserialization with Cloud Firestore.
 *
 * The empty default values are required for Firestore's automatic data mapping.
 *
 * @param userId The unique ID of the user from Firebase Authentication.
 * @param userName The display name of the user.
 * @param totalScore The final total score the user achieved after 75 days.
 * @param completedDate The server-side timestamp of when the user completed the challenge.
 */
data class LeaderboardEntry(
    val userId: String = "",
    val userName: String = "",
    val totalScore: Int = 0,
    @ServerTimestamp val completedDate: Date? = null
)

