package com.example.challengetimer.main

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
import com.example.challengetimer.R
import com.example.challengetimer.database.ChallengeDatabase
import com.example.challengetimer.databinding.MainFragmentBinding

class MainFragment : Fragment() {

    private lateinit var binding: MainFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.main_fragment, container, false)

        val application = requireNotNull(activity).application
        val dataSource = ChallengeDatabase.getInstance(application).challengeDatabaseDao
        val viewModelFactory = MainViewModelFactory(dataSource, application)
        val mainViewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        binding.lifecycleOwner = this
        binding.mainViewModel = mainViewModel
        binding.mainScroll.clipToOutline = true

        val listener = ChallengeListener { challengeId ->
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToTimerFragment(challengeId))
        }
        val adaptersList: List<ChallengeAdapter> = List(4, init =  { index ->
            ChallengeAdapter(listener, index)
        })

        binding.bruhList.challengeList.adapter = adaptersList[0]
        binding.easyList.challengeList.adapter = adaptersList[1]
        binding.mediumList.challengeList.adapter = adaptersList[2]
        binding.hardList.challengeList.adapter = adaptersList[3]

        mainViewModel.challenges.observe(viewLifecycleOwner, Observer {
            it?.let { list ->
                binding.bruhList.outerItemBg.isGone = adaptersList[0].submitNewList(list)
                binding.easyList.outerItemBg.isGone = adaptersList[1].submitNewList(list)
                binding.mediumList.outerItemBg.isGone = adaptersList[2].submitNewList(list)
                binding.hardList.outerItemBg.isGone = adaptersList[3].submitNewList(list)


                binding.emptyTitle.isGone = list.isNotEmpty()
                binding.createFirstButton.isGone = list.isNotEmpty()

                mainViewModel.checkForFailedChallenges()
            }
        })

        mainViewModel.navigateToCreateChallenge.observe(viewLifecycleOwner, Observer { challengeId ->
            challengeId?.let {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToConfigFragment(it))
                mainViewModel.doneConfigNavigation()
            }
        })

        return binding.root
    }
}