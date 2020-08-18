package com.example.challengetimer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.challengetimer.main.MainFragmentDirections
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val navController = findNavController(R.id.nav_host_fragment)
        val fab = findViewById<FloatingActionButton>(R.id.main_fab)
        val bottomAppBar = findViewById<BottomAppBar>(R.id.bottom_app_bar)


        fab.setOnClickListener {
            navController.popBackStack(R.id.mainFragment, false)
        }

        bottomAppBar.setNavigationOnClickListener {
            when (navController.currentDestination?.id) {
                R.id.mainFragment -> navController.navigate(MainFragmentDirections.actionMainFragmentToEditListFragment())
                R.id.configFragment -> {
                    if (navController.previousBackStackEntry?.destination?.id == R.id.editListFragment)
                        navController.popBackStack(R.id.editListFragment, false)
                    else {
                        navController.popBackStack(R.id.mainFragment, false)
                        navController.navigate(MainFragmentDirections.actionMainFragmentToEditListFragment())
                    }
                }
                R.id.timerFragment -> {
                    navController.popBackStack(R.id.mainFragment, false)
                    navController.navigate(MainFragmentDirections.actionMainFragmentToEditListFragment())
                }
            }
        }
    }

    fun getFab(): FloatingActionButton {
        return findViewById(R.id.main_fab)
    }

    fun getBottomAppBar(): BottomAppBar {
        return findViewById(R.id.bottom_app_bar)
    }
}