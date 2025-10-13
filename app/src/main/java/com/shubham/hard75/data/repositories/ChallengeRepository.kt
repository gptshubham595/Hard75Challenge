package com.shubham.hard75.data.repositories

import android.content.Context
import androidx.core.content.edit
import com.shubham.hard75.data.db.ChallengeDao
import com.shubham.hard75.data.db.entities.ChallengeDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

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
     * Fetches the most recently updated day from the database for the current attempt.
     */
    suspend fun getLatestUpdatedDay(): ChallengeDay? {
        val currentAttempt = getCurrentAttemptNumber()
        return dao.getLatestUpdatedDayForAttempt(currentAttempt)
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
     * Increments the attempt number in SharedPreferences.
     */
    suspend fun startNewAttempt() = withContext(Dispatchers.IO) {
        val newAttempt = getCurrentAttemptNumber() + 1
        prefs.edit {
            putInt(ATTEMPT_KEY, newAttempt)
        }
    }

    // The start date logic is no longer needed from SharedPreferences.
    // We are removing getStartDateForCurrentAttempt() and related methods.

    companion object {
        private const val ATTEMPT_KEY = "current_attempt"
    }
}