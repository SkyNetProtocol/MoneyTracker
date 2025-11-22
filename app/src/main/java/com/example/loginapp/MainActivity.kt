package com.example.loginapp

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.loginapp.presentation.home.HomeFragment
import com.example.loginapp.presentation.login.LoginFragment
import com.example.loginapp.presentation.profile.ProfileFragment
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Hide drawer/toolbar on login screen logic would go here if needed,
        // but for now we just handle navigation.
        // Ideally, we should listen to backstack changes to show/hide the drawer toggle.
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Navigate to Home
                // Assuming user ID is stored or accessible. For now, we might just pop back if Home is below.
                // Or replace with HomeFragment if we are on Profile.
                // Ideally, we should use a shared ViewModel or proper Navigation Component.
                // For this manual implementation:
                supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
                // Note: This might pop Login too if not careful.
                // A better approach for manual nav without NavComponent:
                // Check if HomeFragment is already in stack or replace.
                // Since LoginFragment replaces itself with HomeFragment, Home is at root of "logged in" state.
                // So popping back stack might be enough if we added Profile on top.
                
                // Let's just replace for simplicity in this manual setup, assuming we have a user ID.
                // But we don't have user ID easily here without a shared ViewModel.
                // So let's assume HomeFragment is the "base" after login.
                if (supportFragmentManager.findFragmentByTag("HOME") == null) {
                     // If Home is not found (e.g. we are on Profile and Home was replaced), we need to recreate it.
                     // But we need userId.
                     // Let's rely on the fact that we will just hide/show or replace.
                }
                
                // Simplest manual nav for this context:
                // If we are on Profile, just pop it to go back to Home.
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
                if (currentFragment is ProfileFragment) {
                    supportFragmentManager.popBackStack()
                }
            }
            R.id.nav_account_summary -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, com.example.loginapp.presentation.summary.AccountSummaryFragment.newInstance(currentUserId))
                    .commit()
            }
            R.id.nav_profile -> {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
                if (currentFragment !is ProfileFragment) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, ProfileFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }
            R.id.nav_logout -> {
                logout()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun logout() {
        // Clear back stack and go to LoginFragment
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, LoginFragment())
            .commit()
    }
}
