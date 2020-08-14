package com.example.challengetimer.editlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.challengetimer.database.ChallengeDatabaseDao
import com.example.challengetimer.main.MainViewModel

class EditListViewModelFactory(private val database: ChallengeDatabaseDao) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditListViewModel::class.java)) {
            return EditListViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}