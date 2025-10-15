package com.shubham.hard75.data.db.entities

import androidx.room.Entity
import androidx.room.TypeConverters
import com.shubham.hard75.data.db.converters.StringListConverter

/**
 * Represents a single day in a specific challenge attempt.
 * The combination of attemptNumber and dayNumber creates a unique primary key.
 *
 * @param attemptNumber Which attempt this day belongs to (e.g., 1st try, 2nd try).
 * @param dayNumber The day of the challenge (1 to 75).
 * @param status The current status of the day (e.g., COMPLETED, FAILED).
 * @param score The points earned for this day.
 * @param totalTasks The total number of tasks that were required for this day.
 * @param completedTaskIds A list of unique IDs for the tasks that were completed.
 * @param selfieImageUrl The local file URI for the user's selfie for this day.
 * @param selfieNote An optional note the user added to their selfie.
 * @param timestamp The time the day was completed or the selfie was taken.
 */
@Entity(tableName = "challenge_days", primaryKeys = ["attemptNumber", "dayNumber"])
@TypeConverters(StringListConverter::class)
data class ChallengeDay(
    val attemptNumber: Int,
    val dayNumber: Int,
    val status: DayStatus,
    val score: Int = 0,
    val totalTasks: Int,
    val completedTaskIds: List<String> = emptyList(),
    val selfieImageUrl: String? = null,
    val selfieNote: String? = null,
    val timestamp: Long? = null
)


enum class DayStatus {
    LOCKED,      // Future days (Gray)
    FAILED,      // Not started or missed (Red)
    IN_PROGRESS, // Some tasks done (Yellow)
    COMPLETED    // All tasks done (Green)
    ;

    companion object {
        fun getRandomStatus(): DayStatus {
            return entries.toTypedArray().random()
        }

        fun DayStatus.stillHasHope(): Boolean {
            return this == IN_PROGRESS || this == COMPLETED
        }
    }
}