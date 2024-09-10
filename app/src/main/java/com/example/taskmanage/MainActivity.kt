package com.example.taskmanage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private var currentFragment: Fragment? = null
    private lateinit var bottomNavigationView: BottomNavigationView
    private val taskList: MutableList<Task> = mutableListOf() // To store tasks
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.nav_view)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    if (currentFragment !is HomeFragment) {
                        replaceFragment(HomeFragment())
                    }
                    true
                }
                R.id.navigation_progress -> {
                    if (currentFragment !is ProgressFragment) {
                        replaceFragment(ProgressFragment())
                    }
                    true
                }
                R.id.navigation_profile -> {
                    if (currentFragment !is ProfileFragment) {
                        replaceFragment(ProfileFragment())
                    }
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            bottomNavigationView.selectedItemId = R.id.navigation_home
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        currentFragment = fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN) // Optional transition animation
            .commitAllowingStateLoss() // Use commitAllowingStateLoss if you encounter issues with state saving
    }
}
