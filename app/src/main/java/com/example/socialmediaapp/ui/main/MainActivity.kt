package com.example.socialmediaapp.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.socialmediaapp.R
import com.example.socialmediaapp.databinding.ActivityMainBinding
import com.example.socialmediaapp.ui.feed.FeedFragment
import com.example.socialmediaapp.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Default fragment
        loadFragment(FeedFragment())
        showFab { /* default click listener if needed */ }

        // Handle bottom navigation clicks
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_feed -> {
                    loadFragment(FeedFragment())
                    showFab { /* handled in FeedFragment */ }
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    hideFab()
                }
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    /** Show the FAB and assign a click listener */
    fun showFab(clickListener: () -> Unit) {
        binding.fabAddPost.show()
        binding.fabAddPost.setOnClickListener { clickListener() }
    }

    /** Hide the FAB */
    fun hideFab() {
        binding.fabAddPost.hide()
    }
}
