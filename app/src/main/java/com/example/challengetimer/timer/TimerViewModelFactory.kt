package com.example.challengetimer.timer

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.challengetimer.config.ConfigViewModel
import com.example.challengetimer.database.ChallengeDatabaseDao

class TimerViewModelFactory(private val challengeId: Long,
                             private val database: ChallengeDatabaseDao,
                             private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            return TimerViewModel(challengeId, database, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}