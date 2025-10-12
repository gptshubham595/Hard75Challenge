package com.shubham.hard75.data.repositories

import android.content.Context
import androidx.core.content.edit
import com.shubham.hard75.data.db.ChallengeDao
import com.shubham.hard75.data.db.entities.ChallengeDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


/**
 * Repository class that abstracts access to the data source.
 * It is the single source of truth for the challenge data.
 *
 * @param challengeDao The Data Access Object for the challenge_days table.
 */

class ChallengeRepository(
    private val dao: ChallengeDao,
    context: Context
) {
    private val prefs = context.getSharedPreferences("challenge_prefs", Context.MODE_PRIVATE)

    /**
     * Retrieves a flow of all days for the user's current attempt.
     */
    fun getDaysForCurrentAttempt(): Flow<List<ChallengeDay>> {
        val currentAttempt = prefs.getInt(ATTEMPT_KEY, 1)
        return dao.getDaysForAttempt(currentAttempt)
    }

    /**
     * Retrieves a flow of all days from all attempts.
     * Used by the GalleryViewModel.
     */
    fun getAllDays(): Flow<List<ChallengeDay>> {
        return dao.getAllDays()
    }

    /**
     * Fetches a specific day's data for the current attempt.
     */
    suspend fun getDay(dayNumber: Int): ChallengeDay? {
        val currentAttempt = getCurrentAttemptNumber()
        return dao.getDayForAttempt(currentAttempt, dayNumber)
    }

    /**
     * Inserts or updates a ChallengeDay in the database.
     */
    suspend fun upsertDay(day: ChallengeDay) {
        dao.upsertDay(day)
    }

    /**
     * Gets the current attempt number from SharedPreferences.
     */
    suspend fun getCurrentAttemptNumber(): Int = withContext(Dispatchers.IO) {
        prefs.getInt(ATTEMPT_KEY, 1)
    }

    /**
     * Increments the attempt number in SharedPreferences and saves the start date for the new attempt.
     */
    suspend fun startNewAttempt() = withContext(Dispatchers.IO) {
        val newAttempt = getCurrentAttemptNumber() + 1
        prefs.edit {
            putInt(ATTEMPT_KEY, newAttempt)
            putLong(getStartDateKey(newAttempt), System.currentTimeMillis())
        }
    }

    /**
     * Retrieves the start date for the current attempt from SharedPreferences.
     */
    fun getStartDateForCurrentAttempt(): LocalDate? {
        val currentAttempt = prefs.getInt(ATTEMPT_KEY, 1)
        val timestamp = prefs.getLong(getStartDateKey(currentAttempt), 0L)
        return if (timestamp > 0) {
            Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        } else {
            // Fallback for the very first attempt if the date wasn't set yet
            LocalDate.now()
        }
    }

    private fun getStartDateKey(attemptNumber: Int) = "start_date_for_attempt_$attemptNumber"

    companion object {
        private const val ATTEMPT_KEY = "current_attempt"
    }
}
