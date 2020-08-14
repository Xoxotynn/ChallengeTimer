package com.example.challengetimer.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.challengetimer.database.Challenge


@Dao
interface ChallengeDatabaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(challenge: Challenge)

    @Update
    fun update(challenge: Challenge)

    @Query("DELETE FROM challenges_table WHERE challengeId = :key")
    fun deleteChallenge(key: Long)

    @Query("SELECT * FROM challenges_table ORDER BY start_time ASC")
    fun getAllChallenges(): LiveData<List<Challenge>>

    @Query("SELECT * from challenges_table WHERE challengeId = :key")
    fun getChallengeWithId(key: Long): LiveData<Challenge>

    @Query("SELECT * from challenges_table WHERE challengeId = :key")
    fun get(key: Long): Challenge?
}