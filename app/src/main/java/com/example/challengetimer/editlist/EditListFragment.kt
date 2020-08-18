package com.example.challengetimer.editlist

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
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.challengetimer.R
import com.example.challengetimer.database.ChallengeDatabase
import com.example.challengetimer.databinding.EditListFragmentBinding

class EditListFragment : Fragment() {

    private lateinit var binding: EditListFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.edit_list_fragment, container, false)

        val application = requireNotNull(activity).application
        val dataSource = ChallengeDatabase.getInstance(application).challengeDatabaseDao

        val viewModelFactory = EditListViewModelFactory(dataSource)
        val editListViewModel = ViewModelProvider(this, viewModelFactory).get(EditListViewModel::class.java)

        val adapter = EditListAdapter(EditChallengeListener {
                challengeId -> editListViewModel.onConfigChallenge(challengeId)
        }, DeleteChallengeListener{})
        adapter.deleteListener = DeleteChallengeListener { challenge ->
            adapter.deleteItem(adapter.currentList.indexOf(challenge), binding, requireContext())
        }

        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(adapter, requireContext(), binding))

        binding.lifecycleOwner = this
        binding.editListViewModel = editListViewModel
        binding.editChallengeList.adapter = adapter
        binding.editChallengeList.clipToOutline = true
        itemTouchHelper.attachToRecyclerView(binding.editChallengeList)
        binding.editScroll.clipToOutline = true


        editListViewModel.challenges.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)

                binding.editTitle.isGone = it.isEmpty()
                binding.emptyTitle.isGone = it.isNotEmpty()
                binding.createFirstButton.isGone = it.isNotEmpty()
            }
        })

        editListViewModel.navigateToEditChallenge.observe(viewLifecycleOwner, Observer { challengeId ->
            challengeId?.let {
                findNavController().navigate(EditListFragmentDirections.actionEditListFragmentToConfigFragment(it))
                editListViewModel.doneConfigNavigation()
            }
        })

        editListViewModel.navigateToMainFragment.observe(viewLifecycleOwner, Observer {
            if(it) {
                findNavController().popBackStack(R.id.mainFragment, false)
                editListViewModel.doneClosing()
            }
        })

        return binding.root
    }
}