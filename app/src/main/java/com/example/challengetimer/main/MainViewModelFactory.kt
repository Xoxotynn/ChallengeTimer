package com.example.challengetimer.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.challengetimer.database.ChallengeDatabaseDao

class MainViewModelFactory(
    private val database: ChallengeDatabaseDao,
    private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(database, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}