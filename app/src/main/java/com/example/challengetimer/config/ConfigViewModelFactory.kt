package com.example.challengetimer.config

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.challengetimer.database.ChallengeDatabaseDao

class ConfigViewModelFactory(private val challengeId: Long = -1L,
                             private val database: ChallengeDatabaseDao,
                             private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConfigViewModel::class.java)) {
            return ConfigViewModel(challengeId, database, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}