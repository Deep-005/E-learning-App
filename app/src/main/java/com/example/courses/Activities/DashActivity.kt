package com.example.courses.Activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.courses.Fragments.DashFragment
import com.example.courses.Fragments.SearchFragment
import com.example.courses.R
import com.example.courses.databinding.ActivityDashBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class DashActivity : AppCompatActivity() {

    lateinit var binding: ActivityDashBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // initialize firebase
        FirebaseApp.initializeApp(this)
        // Initialize FireStore
        FirebaseFirestore.getInstance()

        // Get the NavController from the FragmentContainerView (NavHostFragment)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragments) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dash -> {
                    navController.navigate(R.id.dashFragment, null, navOptionsWithoutBackStack())
                    true
                }

                R.id.nav_paid -> {
                    navController.navigate(R.id.paidFragment, null, navOptionsWithoutBackStack())
                    true
                }

                R.id.nav_user -> {
                    navController.navigate(R.id.userFragment, null, navOptionsWithoutBackStack())
                    true
                }

                R.id.nav_search -> {
                    navController.navigate(
                        R.id.searchFragment, null, navOptionsWithoutBackStack())
                    true
                }

                R.id.nav_live -> {
                    navController.navigate(
                        R.id.liveFragment, null, navOptionsWithoutBackStack())
                    true
                }

                else -> false
            }
        }

        // Retrieve the target fragment from the Intent from the EnrollmentActivity
        val targetFragment = intent.getStringExtra("targetFragment")

        // Navigate to the desired fragment if specified
        if (targetFragment != null) {
            navigateToFragment(targetFragment)
        }

    }

    private fun navOptionsWithoutBackStack(): NavOptions {
        return NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, true)
            .build()
    }

    // Function to navigate to the specified fragment
    private fun navigateToFragment(fragmentTag: String) {
        val fragment: Fragment = when (fragmentTag) {
            "nav_search" -> SearchFragment()
            else -> DashFragment() // Fallback if no matching fragment
        }

        // Replace the container with the desired fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.parentFrame, fragment)
            .addToBackStack(null)
            .commit()
    }

}