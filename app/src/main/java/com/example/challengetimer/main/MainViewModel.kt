package com.example.challengetimer.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.challengetimer.database.ChallengeDatabaseDao
import kotlinx.coroutines.*

private const val WEEK_MILLIS = 24 * 3600 * 1000 * 7

class MainViewModel(val database: ChallengeDatabaseDao, application: Application) : AndroidViewModel(application) {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val challenges = database.getAllChallenges()

    private val _navigateToCreateChallenge = MutableLiveData<Long>()
    val navigateToCreateChallenge: LiveData<Long>
        get() = _navigateToCreateChallenge

    fun checkForFailedChallenges() {
        uiScope.launch {
            challenges.value?.let {
                for (challenge in it.filter { ch -> ch.rank < 4 }) {
                    val isChallengeFailed =
                        System.currentTimeMillis() - challenge.startTimeMilli >= WEEK_MILLIS * challenge.difficulty.coerceAtLeast(1)
                    if (isChallengeFailed) {
                        deleteChallenge(challenge.challengeId)
                    } else
                        break
                }
            }
        }
    }

    private suspend fun deleteChallenge(challengeId: Long) {
        return withContext(Dispatchers.IO) {
            database.deleteChallenge(challengeId)
        }
    }

    fun onConfigChallenge(challengeId: Long) {
        _navigateToCreateChallenge.value = challengeId
    }

    fun doneConfigNavigation() {
        _navigateToCreateChallenge.value = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}