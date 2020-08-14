package com.example.challengetimer.editlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.challengetimer.database.Challenge
import com.example.challengetimer.database.ChallengeDatabaseDao
import kotlinx.coroutines.*

class EditListViewModel(val database: ChallengeDatabaseDao) : ViewModel() {

    private val viewModelJob = Job()

    val challenges = database.getAllChallenges()

    private val _navigateToEditChallenge = MutableLiveData<Long>()
    val navigateToEditChallenge: LiveData<Long>
        get() = _navigateToEditChallenge

    private val _navigateToMainFragment = MutableLiveData<Boolean>(false)
    val navigateToMainFragment: LiveData<Boolean>
        get() = _navigateToMainFragment

    fun onConfigChallenge(challengeId: Long) {
        _navigateToEditChallenge.value = challengeId
    }

    fun doneConfigNavigation() {
        _navigateToEditChallenge.value = null
    }

    fun onCloseEditList() {
        _navigateToMainFragment.value = true
    }

    fun doneClosing() {
        _navigateToMainFragment.value = false
    }

    suspend fun deleteChallenge(challengeId: Long) {
        withContext(Dispatchers.IO) {
            database.deleteChallenge(challengeId)
        }
    }

    suspend fun insertChallenge(challenge: Challenge) {
        withContext(Dispatchers.IO) {
            database.insert(challenge)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}