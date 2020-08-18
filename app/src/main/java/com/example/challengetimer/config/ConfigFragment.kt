package com.example.challengetimer.config

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.iterator
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.challengetimer.R
import com.example.challengetimer.database.ChallengeDatabase
import com.example.challengetimer.databinding.ConfigFragmentBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.skydoves.balloon.*


class ConfigFragment : Fragment() {

    private lateinit var binding: ConfigFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.config_fragment, container, false)

        val application = requireNotNull(this.activity).application
        val arguments = ConfigFragmentArgs.fromBundle(requireArguments())
        val dataSource = ChallengeDatabase.getInstance(application).challengeDatabaseDao
        val viewModelFactory =
            ConfigViewModelFactory(arguments.challengeId, dataSource, application)
        val configViewModel =
            ViewModelProvider(this, viewModelFactory).get(ConfigViewModel::class.java)

        val balloon = createBalloon(requireContext()) {
            setWidth(170)
            setHeight(64)
            setCornerRadius(24F)
            setAlpha(1f)
            setArrowSize(5)
            setArrowOrientation(ArrowOrientation.LEFT)
            setArrowPosition(0.5f)
            setTextSize(14F)
            setText("Time you will need\nto upgrade your rank")
            setTextColorResource(R.color.primaryTextColor)
            setBackgroundColorResource(R.color.primaryLightColor)
            dismissWhenClicked = true
            dismissWhenTouchOutside = true
            autoDismissDuration = 3500
            setBalloonAnimation(BalloonAnimation.ELASTIC)
            setLifecycleOwner(lifecycleOwner)
            build()
        }

        binding.lifecycleOwner = this
        binding.configViewModel = configViewModel
        binding.configScroll.clipToOutline = true

        binding.infoButton.setOnClickListener {
            balloon.showAlignRight(it)
        }

        binding.colorList.setOnCheckedChangeListener { group, _ ->
            configViewModel.colorChange(setChipColorChecked(group))
        }

        binding.difficultyList.setOnCheckedChangeListener { group, _ ->
            configViewModel.difficultyChange(getDifficultyInt(group))
        }

        binding.editTextTextPersonName.doAfterTextChanged { text ->
            if (text.toString() != configViewModel.challenge.value?.name) {
                configViewModel.nameChange(text.toString())
            }
        }

        configViewModel.challenge.observe(viewLifecycleOwner, Observer {
            binding.saveBtn.isEnabled = !(it.name.isEmpty() || it.color == 0 || it.difficulty == -1)
            if (binding.editTextTextPersonName.text.isEmpty()) {
                binding.editTextTextPersonName.setText(configViewModel.challenge.value?.name)
            }
        })

        configViewModel.onChallengeSavedNavigate.observe(viewLifecycleOwner, Observer { challengeIdArg ->
            challengeIdArg?.let {
                if (it > 0)
                    findNavController().popBackStack()
                else
                    findNavController().popBackStack(R.id.mainFragment, false)

                configViewModel.doneSaveChallenge()
            }
        })

        configViewModel.onChallengeDeletedNavigate.observe(viewLifecycleOwner, Observer { challenge ->
            challenge?.let { ch ->
                val snackbar = Snackbar.make(binding.root,
                    requireContext().getString(R.string.undo_scnackbar_string, ch.name), 2500)
                snackbar.setTextColor(requireContext().getColor(R.color.primaryTextColor))
                snackbar.setBackgroundTint(requireContext().getColor(R.color.primaryDarkColor))
                snackbar.setActionTextColor(requireContext().getColor(R.color.secondaryColor))
                snackbar.setAction("Undo") {
                    configViewModel.undoDelete(ch)
                }
                snackbar.show()

                findNavController().popBackStack(R.id.mainFragment, false)
                configViewModel.doneDeletingChallenge()
            }
        })

        configViewModel.onChallengeReset.observe(viewLifecycleOwner, Observer { challenge ->
            challenge?.let { ch ->
                val snackbar = Snackbar.make(binding.root,
                    requireContext().getString(R.string.reset_scnackbar_string, ch.name), 2500)
                snackbar.setTextColor(requireContext().getColor(R.color.primaryTextColor))
                snackbar.setBackgroundTint(requireContext().getColor(R.color.primaryDarkColor))
                snackbar.setActionTextColor(requireContext().getColor(R.color.secondaryColor))
                snackbar.setAction("Undo") {
                    configViewModel.undoReset(ch)
                }
                snackbar.show()

                configViewModel.doneResettingChallenge()
            }
        })

        when (arguments.challengeId) {
            -1L -> binding.difficultyList.check(R.id.easy_chip)
            -2L -> binding.difficultyList.check(R.id.medium_chip)
            -3L -> binding.difficultyList.check(R.id.hard_chip)
            -4L -> binding.difficultyList.check(R.id.bruh_chip)
            0L -> binding.difficultyList.clearCheck()
            else -> {
                for (item in binding.difficultyList) {
                    item.isClickable = false
                }

                binding.innerDifficultyItemBg.foreground = context?.getDrawable(R.drawable.disabled_difficulty_foreground)
                binding.offText.isGone = false
                binding.difficultyText.isGone = true
                binding.singleDeleteButton.isGone = false
                binding.resetButton.isGone = false
                binding.infoButton.isGone = true
            }
        }

        return binding.root
    }



    private fun getDifficultyInt(group: ChipGroup): Int {
        var index = 0
        for (difficulty in group) {
            if ((difficulty as Chip).isChecked) return index
            index++
        }
        return -1
    }

    private fun setChipColorChecked(group: ChipGroup): Int {
        var colorInt = 0

        for (color in group) {
            val colorChip = color as Chip
            if (colorChip.isChecked) {
                colorChip.setChipStrokeColorResource( when (colorChip.id) {
                    R.id.pink_chip -> R.color.pinkDarkColor
                    R.id.violet_chip -> R.color.violetDarkColor
                    R.id.blue_chip -> R.color.blueDarkColor
                    else -> R.color.primaryLightColor
                })
                colorInt = colorChip.chipBackgroundColor?.defaultColor ?: 0
            }
            else colorChip.setChipStrokeColorResource(R.color.primaryLightColor)
        }

        return colorInt
    }
}