package com.example.challengetimer.editlist

import android.content.Context
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.challengetimer.R
import com.example.challengetimer.database.Challenge
import com.example.challengetimer.databinding.EditChallengeItemBinding
import com.example.challengetimer.databinding.EditListFragmentBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*


class EditListAdapter(val clickListener: EditChallengeListener, var deleteListener: DeleteChallengeListener) :
    ListAdapter<Challenge, EditListAdapter.ViewHolder>(EditChallengeDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(clickListener, deleteListener, item!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    fun deleteItem(position: Int, binding: EditListFragmentBinding,
                   context: Context) {

        val recentlyDeletedItem = getItem(position)
        val deleteJob = Job()
        val uiScope = CoroutineScope(Dispatchers.Main + deleteJob)

        fun undoDelete() {
            uiScope.launch {
                binding.editListViewModel?.insertChallenge(recentlyDeletedItem)
            }
        }

        fun showUndoSnackbar() {

            val snackbar = Snackbar.make(binding.root,
                context.getString(R.string.undo_scnackbar_string, recentlyDeletedItem.name), 2500)
            snackbar.setTextColor(context.getColor(R.color.primaryTextColor))
            snackbar.setBackgroundTint(context.getColor(R.color.primaryDarkColor))
            snackbar.setActionTextColor(context.getColor(R.color.secondaryColor))
            snackbar.setAction("Undo") {
                undoDelete()
            }
            snackbar.show()
        }

        uiScope.launch {
            binding.editListViewModel?.deleteChallenge(recentlyDeletedItem.challengeId)
        }
        showUndoSnackbar()
    }

    class ViewHolder private constructor(val binding: EditChallengeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(clickListener: EditChallengeListener, deleteListener: DeleteChallengeListener, item: Challenge) {
            binding.challenge = item
            binding.clickListener = clickListener
            binding.deleteListener = deleteListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = EditChallengeItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}


class EditChallengeDiffCallback :
    DiffUtil.ItemCallback<Challenge>() {
    override fun areItemsTheSame(oldItem: Challenge, newItem: Challenge): Boolean {
        return (oldItem.challengeId == newItem.challengeId)
    }

    override fun areContentsTheSame(oldItem: Challenge, newItem: Challenge): Boolean {
        return (oldItem == newItem)
    }
}


class EditChallengeListener(val clickListener: (challengeId: Long) -> Unit) {
    fun onClick(challenge: Challenge) = clickListener(challenge.challengeId)
}

class DeleteChallengeListener(val clickListener: (challenge: Challenge) -> Unit) {
    fun onClick(challenge: Challenge) = clickListener(challenge)
}


class SwipeToDeleteCallback(
    adapter: EditListAdapter,
    context: Context,
    binding: EditListFragmentBinding) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val myAdapter = adapter
    private val myBinding = binding
    private val myContext = context

    private val icon = ContextCompat.getDrawable(context, R.drawable.delete_pink_image)
    private val background = ContextCompat.getDrawable(context, R.drawable.swipe_delete_background)


    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        myAdapter.deleteItem(position, myBinding, myContext)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder): Boolean { return true }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView = viewHolder.itemView
        val bgCornerOffset = 128
        val iconMargin: Int = (itemView.height - icon?.intrinsicHeight!!) / 2;
        val iconTop: Int = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
        val iconBottom: Int = iconTop + icon.intrinsicHeight

        if (dX < 0) { // Swiping to the left
            val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth - 32
            val iconRight = itemView.right - iconMargin - 32
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

            background?.setBounds(
                itemView.right + dX.toInt() - bgCornerOffset,
                itemView.top,
                itemView.right,
                itemView.bottom
            )
        } else { // view is unSwiped
            background?.setBounds(0, 0, 0, 0)
            icon.setBounds(0, 0, 0, 0)
        }

        background?.draw(c)
        icon.draw(c)
    }
}