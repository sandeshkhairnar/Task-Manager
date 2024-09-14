package com.example.taskmanage

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private var currentFragment: Fragment? = null
    private lateinit var bottomNavigationView: BottomNavigationView
    private var backPressedTime: Long = 0
    private val BACK_PRESS_INTERVAL: Long = 2000 // 2 seconds

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
        } else {
            bottomNavigationView.selectedItemId = savedInstanceState.getInt("selectedItemId", R.id.navigation_home)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedItemId", bottomNavigationView.selectedItemId)
    }

    private fun replaceFragment(fragment: Fragment) {
        currentFragment = fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        when (currentFragment) {
            is HomeFragment -> {
                if (backPressedTime + BACK_PRESS_INTERVAL > System.currentTimeMillis()) {
                    super.onBackPressed()
                    finish()
                } else {
                    Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
                }
                backPressedTime = System.currentTimeMillis()
            }
            else -> {
                bottomNavigationView.selectedItemId = R.id.navigation_home
            }
        }
    }

    override fun onResume() {
        super.onResume()
        when (currentFragment) {
            is HomeFragment -> bottomNavigationView.selectedItemId = R.id.navigation_home
            is ProgressFragment -> bottomNavigationView.selectedItemId = R.id.navigation_progress
            is ProfileFragment -> bottomNavigationView.selectedItemId = R.id.navigation_profile
        }
    }
}