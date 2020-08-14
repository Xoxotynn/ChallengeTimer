package com.example.challengetimer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenges_table")
data class Challenge (

    @PrimaryKey(autoGenerate = true)
    var challengeId: Long = 0L,

    @ColumnInfo(name = "challenge_name")
    var name: String = "",

    @ColumnInfo(name = "start_time")
    var startTimeMilli: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "challenge_progress")
    var progressMilli: Long = 0L,

    @ColumnInfo(name = "challenge_rank")
    var rank: Int = 0,

    @ColumnInfo(name = "challenge_difficulty")
    var difficulty: Int = 0,

    @ColumnInfo(name = "challenge_color")
    var color: Int = 0
)