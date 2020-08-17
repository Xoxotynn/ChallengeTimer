package com.example.challengetimer.main

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.get
import androidx.core.view.size
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.challengetimer.R
import com.example.challengetimer.database.Challenge
import com.example.challengetimer.databinding.ChallengeItemBinding
import kotlinx.android.synthetic.main.main_fragment.view.*
import kotlin.properties.Delegates

class ChallengeAdapter(val clickListener: ChallengeListener, val difficulty: Int = 0) : ListAdapter<Challenge, ChallengeAdapter.ViewHolder>(ChallengeDiffCallback()) {

    fun submitNewList(list: List<Challenge>?): Boolean {
        var newList: List<Challenge> = listOf()
        list?.let {
            newList = it.filter { ch -> ch.difficulty == difficulty }
        }
        if (newList.isNotEmpty())
            submitList(newList)

        return newList.isEmpty()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(clickListener, item!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ChallengeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(clickListener: ChallengeListener, item: Challenge) {
            binding.challenge = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ChallengeItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class ChallengeDiffCallback :
    DiffUtil.ItemCallback<Challenge>() {
    override fun areItemsTheSame(oldItem: Challenge, newItem: Challenge): Boolean {
        return (oldItem.challengeId == newItem.challengeId)
    }

    override fun areContentsTheSame(oldItem: Challenge, newItem: Challenge): Boolean {
        return (oldItem == newItem)
    }
}

class ChallengeListener(val clickListener: (challengeId: Long) -> Unit) {
    fun onClick(challenge: Challenge) = clickListener(challenge.challengeId)
}