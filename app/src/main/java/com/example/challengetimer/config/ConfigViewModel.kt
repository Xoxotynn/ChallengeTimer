package com.example.challengetimer.config

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.challengetimer.database.Challenge
import com.example.challengetimer.database.ChallengeDatabaseDao
import kotlinx.coroutines.*
import java.util.*

class ConfigViewModel(
    private val challengeId: Long = -1L,
    val database: ChallengeDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    private val _challenge = MutableLiveData<Challenge>(Challenge())
    val challenge: LiveData<Challenge>
        get() = _challenge

    private val _onChallengeSavedNavigate = MutableLiveData<Long>()
    val onChallengeSavedNavigate: LiveData<Long>
    get() = _onChallengeSavedNavigate

    private val _onChallengeDeletedNavigate = MutableLiveData<Challenge>()
    val onChallengeDeletedNavigate: LiveData<Challenge>
        get() = _onChallengeDeletedNavigate

    private val _onChallengeReset = MutableLiveData<Challenge>()
    val onChallengeReset: LiveData<Challenge>
        get() = _onChallengeReset

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init {
        onFillConfigInfo()
    }

    private fun onFillConfigInfo() {
        uiScope.launch {
            if (challengeId > 0) {
                _challenge.value = getChallenge(challengeId)
            }
        }
    }

    fun onSave() {
        uiScope.launch {
            _challenge.value?.let {
                if (challengeId > 0) {
                    it.challengeId = challengeId
                    updateChallenge(it)
                } else  {
                    it.startTimeMilli = System.currentTimeMillis()
                    insertChallenge(it)
                }
            }

            _onChallengeSavedNavigate.value = challengeId
        }
    }

    fun onReset() {
        uiScope.launch {
            _challenge.value?.let {
                getChallenge(it.challengeId)?.let { newChallenge ->
                    newChallenge.progressMilli = 0L
                    newChallenge.rank = 0
                    newChallenge.startTimeMilli = System.currentTimeMillis()
                    updateChallenge(newChallenge)
                }
            }
        }
        _onChallengeReset.value = _challenge.value
    }

    fun undoReset(challenge: Challenge) {
        uiScope.launch {
            updateChallenge(challenge)
        }
    }

    fun onDelete() {
        uiScope.launch {
            deleteChallenge(challenge.value?.challengeId!!)
        }

        _onChallengeDeletedNavigate.value = _challenge.value
    }

    fun undoDelete(challenge: Challenge) {
        uiScope.launch {
            insertChallenge(challenge)
        }
    }

    fun onClose() {
        _onChallengeSavedNavigate.value = challengeId
    }

    fun nameChange(newName: String) {
        _challenge.value?.name = newName.capitalize()
        _challenge.value = _challenge.value
    }

    fun difficultyChange(newDifficulty: Int) {
        _challenge.value?.difficulty = newDifficulty
        _challenge.value = _challenge.value
    }

    fun colorChange(newColor: Int) {
        _challenge.value?.color = newColor
        _challenge.value = _challenge.value
    }

    fun doneResettingChallenge() {
        _onChallengeReset.value = null
    }

    fun doneDeletingChallenge() {
        _onChallengeDeletedNavigate.value = null
    }

    fun doneSaveChallenge() {
        _onChallengeSavedNavigate.value = null
    }


    private suspend fun getChallenge(challengeId: Long): Challenge? {
        return withContext(Dispatchers.IO) {
            database.get(challengeId)
        }
    }

    private suspend fun deleteChallenge(challengeId: Long) {
        withContext(Dispatchers.IO) {
            database.deleteChallenge(challengeId)
        }
    }

    private suspend fun insertChallenge(challenge: Challenge) {
        withContext(Dispatchers.IO) {
            database.insert(challenge)
        }
    }

    private suspend fun updateChallenge(challenge: Challenge) {
        withContext(Dispatchers.IO) {
            database.update(challenge)
        }
    }
}