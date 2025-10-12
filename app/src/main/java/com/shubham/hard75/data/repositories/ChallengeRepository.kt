package com.shubham.hard75.data.repositories

import com.shubham.hard75.data.db.ChallengeDao
import com.shubham.hard75.data.db.entities.ChallengeDay
import kotlinx.coroutines.flow.Flow


/**
 * Repository class that abstracts access to the data source.
 * It is the single source of truth for the challenge data.
 *
 * @param challengeDao The Data Access Object for the challenge_days table.
 */
class ChallengeRepository(private val challengeDao: ChallengeDao) {

    /**
     * Retrieves all challenge days from the database as a Flow.
     * The Flow will automatically emit new values whenever the data changes.
     */
    fun getAllDays(): Flow<List<ChallengeDay>> {
        return challengeDao.getAllDays()
    }

    /**
     * Retrieves a single challenge day by its number.
     * This is a suspending function and should be called from a coroutine.
     *
     * @param dayNumber The number of the day to retrieve (1-75).
     * @return The ChallengeDay object, or null if not found.
     */
    suspend fun getDay(dayNumber: Int): ChallengeDay? {
        return challengeDao.getDay(dayNumber)
    }

    /**
     * Inserts or updates a challenge day in the database.
     * If a day with the same dayNumber already exists, it will be replaced.
     * This is a suspending function.
     *
     * @param day The ChallengeDay object to save.
     */
    suspend fun upsertDay(day: ChallengeDay) {
        challengeDao.upsertDay(day)
    }

    /**
     * Deletes all entries from the challenge_days table.
     * Used to reset the entire challenge.
     * This is a suspending function.
     */
    suspend fun resetChallenge() {
        challengeDao.resetChallenge()
    }
}
