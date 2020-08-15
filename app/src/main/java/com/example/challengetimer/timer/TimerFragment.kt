package com.example.challengetimer.timer

import android.animation.ObjectAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.challengetimer.MainActivity
import com.example.challengetimer.R
import com.example.challengetimer.database.ChallengeDatabase
import com.example.challengetimer.databinding.TimerFragmentBinding
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.createBalloon

class TimerFragment : Fragment() {

    private lateinit var binding: TimerFragmentBinding
    private lateinit var listener: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.timer_fragment, container, false)

        val application = requireNotNull(this.activity).application
        val arguments = TimerFragmentArgs.fromBundle(requireArguments())
        val dataSource = ChallengeDatabase.getInstance(application).challengeDatabaseDao
        val viewModelFactory = TimerViewModelFactory(arguments.challengeId, dataSource, application)
        val timerViewModel = ViewModelProvider(this, viewModelFactory).get(TimerViewModel::class.java)
        var balloon = createBalloon(requireContext()) {}

        listener = (activity as MainActivity?)!!
        val fab = listener.getFab()
        val bottomAppBar = listener.getBottomAppBar()

        binding.lifecycleOwner = this
        binding.timerViewModel = timerViewModel
        binding.timerSeekBar.setOnSeekBarChangeListener(timerViewModel)

        binding.difficultyImage.setOnClickListener {
            balloon.showAlignBottom(it)
        }

        timerViewModel.infoString.observe(viewLifecycleOwner, Observer {
            balloon = createBalloon(requireContext()) {
                setWidth(204)
                setHeight(64)
                setCornerRadius(24F)
                setAlpha(1f)
                setArrowSize(5)
                setArrowOrientation(ArrowOrientation.TOP)
                setArrowPosition(0.5f)
                setTextSize(14F)
                setText(it)
                setTextColorResource(R.color.primaryTextColor)
                setBackgroundColorResource(R.color.primaryLightColor)
                dismissWhenClicked = true
                dismissWhenTouchOutside = true
                autoDismissDuration = 3500
                setBalloonAnimation(BalloonAnimation.ELASTIC)
                setLifecycleOwner(lifecycleOwner)
                build()
            }
        })

        timerViewModel.onTimerClosedNavigate.observe(viewLifecycleOwner, Observer {
            if (it) {
                findNavController().popBackStack(R.id.mainFragment, false)
                timerViewModel.doneClosing()
            }
        })

        timerViewModel.navigateToEditChallenge.observe(viewLifecycleOwner, Observer { challengeId ->
            challengeId?.let {
                findNavController().navigate(TimerFragmentDirections.actionTimerFragmentToConfigFragment(it))
                timerViewModel.doneConfigNavigation()
            }
        })

        timerViewModel.targetTime.observe(viewLifecycleOwner, Observer { time ->
            if (time != null) {
                uiTimerListener(true, fab, bottomAppBar, binding.timerSeekBar.progress)
                timerViewModel.setTimer()
            } else {
                uiTimerListener(false, fab, bottomAppBar,
                    (timerViewModel.currentTime.value?.div(300)?.minus(1))?.toFloat()!!)
            }
        })

        createChannel(getString(R.string.timer_notification_channel_id), getString(R.string.timer_notification_channel_name))
        return binding.root
    }

    private fun uiTimerListener(isStarted: Boolean, fab: FloatingActionButton, appBar: BottomAppBar, progress: Float) {
        binding.cancelTimerButton.isGone = !isStarted
        fab.isGone = isStarted
        appBar.isGone = isStarted
        binding.closeButton.isGone = isStarted
        binding.configButton.isGone = isStarted
        binding.difficultyImage.isGone = isStarted
        binding.remainingText.isGone = isStarted
        binding.timerSeekBar.isEnabled = !isStarted
        binding.startTimerButton.isGone = isStarted
        binding.timerSeekBar.pointerStrokeWidth = if (isStarted) 0F else 72F

        if (isStarted) {
            ObjectAnimator.ofFloat(binding.timerSeekBar, "progress",
                progress, binding.timerSeekBar.max)
                .setDuration(500)
                .start()


            ObjectAnimator.ofFloat(binding.rankImage, "translationY", 50F)
                .setDuration(500)
                .start()

        } else {
            ObjectAnimator.ofFloat(binding.timerSeekBar, "progress",
                binding.timerSeekBar.max, progress)
                .setDuration(500)
                .start()

            ObjectAnimator.ofFloat(binding.rankImage, "translationY", 0F)
                .setDuration(500)
                .start()
        }
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
                .apply {
                    setShowBadge(false)
                }
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.WHITE
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Timer finished!"

            val notificationManager = requireActivity().getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener.getBottomAppBar().isGone = false
        listener.getFab().isGone = false
    }
}