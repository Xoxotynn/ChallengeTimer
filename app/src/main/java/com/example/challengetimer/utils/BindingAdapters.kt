package com.example.challengetimer.utils

import android.animation.ObjectAnimator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.iterator
import androidx.databinding.BindingAdapter
import com.example.challengetimer.R
import com.example.challengetimer.database.Challenge
import com.example.challengetimer.timer.TimeConst
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import me.tankery.lib.circularseekbar.CircularSeekBar

@BindingAdapter("listDifficultyText")
fun TextView.setDifficultyText(difficulty: Long?) {
    difficulty?.let {
        text = when (difficulty) {
            0L -> "Bruh"
            -1L -> "Easy"
            -2L -> "Medium"
            -3L -> "Hard"
            else -> "Bruh"
        }
    }
}

@BindingAdapter("listDifficultyImage")
fun ImageView.setDifficultyImage(difficulty: Long?) {
    difficulty?.let {
        setImageResource(when (it) {
            0L -> R.drawable.bruh_image
            -1L -> R.drawable.rank_zero_pink_image
            -2L -> R.drawable.rank_zero_blue_image
            -3L -> R.drawable.rank_zero_violet_image
            else -> R.drawable.bruh_image
        })
    }
}

@BindingAdapter("difficultyImage")
fun ImageView.setDifficultyImage(item: Challenge?) {
    item?.let {
        setImageResource(when (it.difficulty) {
            0 -> R.drawable.bruh_image
            1 -> R.drawable.rank_zero_pink_image
            2 -> R.drawable.rank_zero_blue_image
            3 -> R.drawable.rank_zero_violet_image
            else -> R.drawable.bruh_image
        })
    }
}

@BindingAdapter("challengeImage")
fun ImageView.setRankImage(item: Challenge?) {
    item?.let {
        setImageResource(when (it.rank) {
            0 -> R.drawable.rank_silver_image
            1 -> R.drawable.rank_platina_image
            2 -> R.drawable.rank_diamond_image
            3 -> R.drawable.rank_grand_image
            else -> when (it.color) {
                ContextCompat.getColor(context, R.color.pinkColor) -> R.drawable.rank_zero_pink_image
                ContextCompat.getColor(context, R.color.violetColor) -> R.drawable.rank_zero_violet_image
                ContextCompat.getColor(context, R.color.blueColor) -> R.drawable.rank_zero_blue_image
                else -> R.color.transparentColor
            }
        })
    }
}

@BindingAdapter("challengeBackground")
fun ImageView.setRankBackground(item: Challenge?) {
    item?.let {
        setBackgroundResource(when(it.color) {
            ContextCompat.getColor(context, R.color.pinkColor) -> R.drawable.color_pink_background
            ContextCompat.getColor(context, R.color.violetColor) -> R.drawable.color_violet_background
            ContextCompat.getColor(context, R.color.blueColor) -> R.drawable.color_blue_background
            else -> R.color.transparentColor
        })
    }
}

@BindingAdapter("checkedColor")
fun setCheckedColor(colorList: ChipGroup, item: Challenge?) {
    item?.let {
        for (colorView in colorList) {
            val chipColor = (colorView as Chip).chipBackgroundColor?.defaultColor
            if (it.color == chipColor) {
                (colorView).isChecked = true
                break
            }
        }
    }
}

@BindingAdapter("checkedDifficulty")
fun setCheckedDifficulty(difficultyList: ChipGroup, item: Challenge?) {
    item?.let {
        when (it.difficulty) {
            0 -> difficultyList.check(R.id.bruh_chip)
            1 -> difficultyList.check(R.id.easy_chip)
            2 -> difficultyList.check(R.id.medium_chip)
            3 -> difficultyList.check(R.id.hard_chip)
        }
    }
}

@BindingAdapter("progressColor")
fun CircularSeekBar.setProgressColor(item: Challenge?) {
    item?.let {
        circleFillColor = it.color
        circleProgressColor = when(it.color) {
            ContextCompat.getColor(context, R.color.pinkColor) -> ContextCompat.getColor(context, R.color.pinkDarkColor)
            ContextCompat.getColor(context, R.color.violetColor) -> ContextCompat.getColor(context, R.color.violetDarkColor)
            ContextCompat.getColor(context, R.color.blueColor) -> ContextCompat.getColor(context, R.color.blueDarkColor)
            else -> R.color.transparentColor
        }
    }
}

@BindingAdapter("maxAndProgress")
fun CircularSeekBar.setValues(item: Challenge?) {
    item?.let {
        val progressInMins = (it.progressMilli / 60000).toInt()
        val targetTime = calculateTargetTime(it.rank, it.difficulty, progressInMins)
        val prevRankTargetTime = calculateTargetTime(it.rank - 1, it.difficulty, progressInMins)

        max = (targetTime - prevRankTargetTime).toFloat()
        ObjectAnimator.ofFloat(this, "progress",
            progress, (progressInMins - prevRankTargetTime).coerceAtLeast(0).toFloat())
            .setDuration(500)
            .start()
    }
}

@BindingAdapter("remainingTimeText")
fun TextView.setRemaining(item: Challenge?) {
    item?.let {
        val progressInMins = (it.progressMilli / 60000).toInt()
        val targetTime = calculateTargetTime(it.rank, it.difficulty, progressInMins)
        val resultString: String

        if (it.rank < 4) {
            val remainingTimeInMins = targetTime - progressInMins
            val remainingHrs = remainingTimeInMins / 60
            val remainingMins = remainingTimeInMins.rem(60)

            resultString = if (remainingHrs != 0 && remainingMins != 0) {
                "$remainingHrs hours $remainingMins minutes until the next rank"
            } else if (remainingHrs == 0 && remainingMins != 0) {
                "$remainingMins minutes until the next rank"
            } else if (remainingHrs != 0 && remainingMins == 0) {
                "$remainingHrs hours until the next rank"
            } else
                "Time to get the next rank!"

        } else {
            resultString = context.getString(R.string.last_rank_remaining_string)
        }

        text = resultString
    }
}


// Util functions
fun calculateTargetTime(rank: Int, difficulty: Int, progressInMins: Int): Int {
    return if (rank < 4) {
        val dffclt = difficulty.coerceAtLeast(1)
        if (rank != 0) {
            TimeConst.DIFFICULTY_CONST.time * dffclt * (rank + 1) * rank
        } else {
            dffclt.times(TimeConst.ZERO_RANK_CONST.time)
        }
    } else
        progressInMins
}