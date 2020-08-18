package com.example.challengetimer.timer

import android.app.Application
import android.app.NotificationManager
import android.os.CountDownTimer
import android.text.format.DateUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.challengetimer.R
import com.example.challengetimer.database.Challenge
import com.example.challengetimer.database.ChallengeDatabaseDao
import com.example.challengetimer.utils.sendNotification
import kotlinx.coroutines.*
import me.tankery.lib.circularseekbar.CircularSeekBar
import kotlin.math.max

enum class Difficulty {
    BRUH, EASY, MEDIUM, HARD
}

enum class TimeConst(val time: Int) {
    ZERO_RANK_CONST(30),
    DIFFICULTY_CONST(150),
    WEEK_MILLIS(24*3600*1000*7)
}

class TimerViewModel(
    private val challengeId: Long,
    val database: ChallengeDatabaseDao,
    private val app: Application
) : AndroidViewModel(app), CircularSeekBar.OnCircularSeekBarChangeListener  {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    val notificationManager = ContextCompat.getSystemService(app, NotificationManager::class.java) as NotificationManager

    private lateinit var timer: CountDownTimer

    val challenge = database.getChallengeWithId(challengeId)

    private val _onTimerClosedNavigate = MutableLiveData<Boolean>()
    val onTimerClosedNavigate: LiveData<Boolean>
        get() = _onTimerClosedNavigate

    private val _navigateToEditChallenge = MutableLiveData<Long>()
    val navigateToEditChallenge: LiveData<Long>
        get() = _navigateToEditChallenge

    private val _currentTime = MutableLiveData(300)
    val currentTime: LiveData<Int>
        get() = _currentTime

    val currentTimeString = Transformations.map(currentTime) { time ->
        DateUtils.formatElapsedTime(time.toLong())
    }

    val infoString = Transformations.map(challenge) {
        var remainingDays = (TimeConst.WEEK_MILLIS.time * it.difficulty.coerceAtLeast(1)) - (System.currentTimeMillis() - it.startTimeMilli)
        remainingDays = remainingDays.div(TimeConst.WEEK_MILLIS.time/7) + 1
        app.applicationContext.getString(R.string.current_difficulty_tooltip, Difficulty.values()[it.difficulty], remainingDays)
    }

    private val _targetTime = MutableLiveData<Int>()
    val targetTime: LiveData<Int>
        get() = _targetTime

    init {

    }

    fun setTimer() {
        var countDownTime: Long
        currentTime.value?.let {
            countDownTime = if (challenge.value?.difficulty == 0) 10000L else it.times(1000).toLong()
            notificationManager.cancelAll()
            createTimer(countDownTime)
        }
    }

    private fun createTimer(time: Long) {
        val target = targetTime.value
        uiScope.launch {
            timer = object : CountDownTimer(time, 1000) {
                override fun onFinish() {
                    uiScope.launch {
                        challenge.value?.let { ch ->
                            ch.progressMilli += target?.times(1000L)!!
                            ch.rank = checkRank(ch)
                            update(ch)
                        }
                    }
                    onCancelTimer()
                    notificationManager.sendNotification(challenge.value, app.getString(R.string.timer_finished), app)
                }

                override fun onTick(millisUntilFinished: Long) {
                    _currentTime.value = (millisUntilFinished / 1000).toInt()
                }
            }.start()
        }
    }

    fun onStartTimer() {
        _targetTime.value = _currentTime.value
    }

    fun onCancelTimer() {
        _targetTime.value?.let {
            _currentTime.value = it
            _targetTime.value = null
            timer.cancel()
        }
    }

    fun onClose() {
        _onTimerClosedNavigate.value = true
    }

    fun doneClosing() {
        _onTimerClosedNavigate.value = false
    }

    fun onConfigChallenge() {
        _navigateToEditChallenge.value = challengeId
    }

    fun doneConfigNavigation() {
        _navigateToEditChallenge.value = null
    }

    fun checkRank(item: Challenge): Int {
        val progressInMins = (item.progressMilli / 60000).toInt()
        var currentRank: Int

        // set difficulty to 1 if it 0(bruh roflanTest difficulty)
        val dffclt = item.difficulty.coerceAtLeast(1)

        // check for the first rank reaching
        currentRank = max((progressInMins / dffclt.times(TimeConst.ZERO_RANK_CONST.time)).coerceIn(0, 1), item.rank)

        // if first rank reached check for higher ranks
        if (currentRank > 0) {
            val rankDivisionConst = TimeConst.DIFFICULTY_CONST.time * dffclt * currentRank
            currentRank = (progressInMins / rankDivisionConst).coerceIn(currentRank, 4)
        }

        return currentRank
    }

    private suspend fun update(item: Challenge) {
        return withContext(Dispatchers.IO) {
            database.update(item)
        }
    }

    override fun onProgressChanged(circularSeekBar: CircularSeekBar?, progress: Float, fromUser: Boolean) {
        if (fromUser)
            _currentTime.value = (progress.toInt() + 1) * 300
    }
    override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {}
    override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {}

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}