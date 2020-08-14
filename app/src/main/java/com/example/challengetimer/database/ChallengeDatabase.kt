package com.example.challengetimer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Challenge::class], version = 14, exportSchema = false)
abstract class ChallengeDatabase : RoomDatabase() {

    abstract val challengeDatabaseDao: ChallengeDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: ChallengeDatabase? = null

        fun getInstance(context: Context): ChallengeDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ChallengeDatabase::class.java,
                        "challenges_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}