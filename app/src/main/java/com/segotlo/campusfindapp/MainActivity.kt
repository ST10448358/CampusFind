package com.segotlo.campusfindapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var lastDestinationId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lastDestinationId = savedInstanceState?.getInt("LAST_DESTINATION", 0) ?: 0

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MockRepository.init(applicationContext)

        val navHostFragment = binding.navHostFragment.getFragment<NavHostFragment>()
        val navController = navHostFragment.navController

        if (lastDestinationId != 0 && lastDestinationId != R.id.openingFragment) {
            try {
                navController.navigate(lastDestinationId)
            } catch (_: Throwable) {}
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val navOptions = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(R.id.homeFragment, inclusive = false, saveState = true)
                .build()

            try {
                navController.navigate(item.itemId, null, navOptions)
                true
            } catch (_: Exception) {
                false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            lastDestinationId = destination.id
            when (destination.id) {
                R.id.homeFragment,
                R.id.searchFragment,
                R.id.myReportsFragment,
                R.id.notificationsFragment,
                R.id.profileFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                    binding.bottomNavigation.menu.findItem(destination.id)?.isChecked = true
                }
                else -> {
                    binding.bottomNavigation.visibility = View.GONE
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("LAST_DESTINATION", lastDestinationId)
    }
}
