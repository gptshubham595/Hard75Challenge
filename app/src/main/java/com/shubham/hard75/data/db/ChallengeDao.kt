package com.shubham.hard75.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shubham.hard75.data.db.entities.ChallengeDay
import kotlinx.coroutines.flow.Flow


@Dao
interface ChallengeDao {

    /**
     * Inserts or updates a day's data in the database.
     * If a day with the same primary key (attemptNumber, dayNumber) exists, it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDay(day: ChallengeDay)

    /**
     * Retrieves a flow of all days for a specific attempt, ordered by day number.
     * This is used to display the calendar for the current challenge.
     * @param attemptNumber The attempt to fetch days for.
     */
    @Query("SELECT * FROM challenge_days WHERE attemptNumber = :attemptNumber ORDER BY dayNumber ASC")
    fun getDaysForAttempt(attemptNumber: Int): Flow<List<ChallengeDay>>

    /**
     * Fetches a single day's data for a specific attempt.
     * @param attemptNumber The attempt number.
     * @param dayNumber The day number within the attempt.
     */
    @Query("SELECT * FROM challenge_days WHERE attemptNumber = :attemptNumber AND dayNumber = :dayNumber")
    suspend fun getDayForAttempt(attemptNumber: Int, dayNumber: Int): ChallengeDay?

    /**
     * Retrieves a flow of all days from all attempts.
     * This is useful for the Gallery screen to show a complete history.
     */
    @Query("SELECT * FROM challenge_days ORDER BY attemptNumber ASC, dayNumber ASC")
    fun getAllDays(): Flow<List<ChallengeDay>>
}