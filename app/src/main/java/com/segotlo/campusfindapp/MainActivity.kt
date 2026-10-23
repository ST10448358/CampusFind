package com.segotlo.campusfindapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.databinding.ActivityMainBinding
import com.segotlo.campusfindapp.ui.xml.CampusBotDialog

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

        binding.btnFloatingAskAssistant.setOnClickListener {
            CampusBotDialog().show(supportFragmentManager, "CampusBotDialog")
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
                    if (binding.bottomNavigation.tag != "student") {
                        binding.bottomNavigation.tag = "student"
                        binding.bottomNavigation.menu.clear()
                        binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu)
                    }
                    binding.bottomNavigation.visibility = View.VISIBLE
                    binding.bottomNavigation.menu.findItem(destination.id)?.isChecked = true
                    binding.btnFloatingAskAssistant.visibility = View.VISIBLE
                }
                R.id.adminDashboardFragment,
                R.id.adminReportsFragment,
                R.id.adminMatchesFragment,
                R.id.adminUsersFragment,
                R.id.adminProfileFragment -> {
                    if (binding.bottomNavigation.tag != "admin") {
                        binding.bottomNavigation.tag = "admin"
                        binding.bottomNavigation.menu.clear()
                        binding.bottomNavigation.inflateMenu(R.menu.admin_bottom_nav_menu)
                    }
                    binding.bottomNavigation.visibility = View.VISIBLE
                    binding.bottomNavigation.menu.findItem(destination.id)?.isChecked = true
                    binding.btnFloatingAskAssistant.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavigation.visibility = View.GONE
                    binding.btnFloatingAskAssistant.visibility = View.GONE
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("LAST_DESTINATION", lastDestinationId)
    }
}
