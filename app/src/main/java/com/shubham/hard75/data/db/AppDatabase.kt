package com.shubham.hard75.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shubham.hard75.data.db.entities.ChallengeDay

@Database(entities = [ChallengeDay::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun challengeDao(): ChallengeDao

    companion object {
        // Volatile annotation ensures that the instance is always up-to-date and the same for all execution threads.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Return the existing instance if it's not null, otherwise create a new database instance.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "75_hard_challenge_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
