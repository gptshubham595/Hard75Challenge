package com.shubham.hard75.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.shubham.hard75.data.db.converters.StringListConverter

/**
 * Represents the state of a single day in the 75 Hard challenge.
 *
 * @param dayNumber The day of the challenge (1-75).
 * @param status The current status of the day (Locked, Failed, In Progress, Completed).
 * @param completedTasks A bitmask representing which tasks are completed.
 * @param score The points earned for this day.
 */
@Entity(tableName = "challenge_days")
@TypeConverters(StringListConverter::class)
data class ChallengeDay(
    @PrimaryKey val dayNumber: Int,
    val status: DayStatus,
    val score: Int = 0,
    val completedTaskIds: List<String> = emptyList(),
    val totalTasks: Int = 9 // You can adjust this if needed
)

enum class DayStatus {
    LOCKED,      // Future days (Gray)
    FAILED,      // Not started or missed (Red)
    IN_PROGRESS, // Some tasks done (Yellow)
    COMPLETED    // All tasks done (Green)
}