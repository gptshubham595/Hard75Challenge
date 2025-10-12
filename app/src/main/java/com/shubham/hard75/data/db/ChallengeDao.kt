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
     * Inserts or updates a ChallengeDay in the database.
     * If a day with the same dayNumber already exists, it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDay(day: ChallengeDay)

    /**
     * Retrieves all challenge days from the database, ordered by day number.
     * Returns a Flow for reactive updates.
     */
    @Query("SELECT * FROM challenge_days ORDER BY dayNumber ASC")
    fun getAllDays(): Flow<List<ChallengeDay>>

    /**
     * Retrieves a single challenge day by its number.
     */
    @Query("SELECT * FROM challenge_days WHERE dayNumber = :dayNumber LIMIT 1")
    suspend fun getDay(dayNumber: Int): ChallengeDay?

    /**
     * Deletes all entries from the challenge_days table.
     * Used to reset the challenge.
     */
    @Query("DELETE FROM challenge_days")
    suspend fun resetChallenge()
}
