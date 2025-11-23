package com.example.loginapp

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.loginapp.presentation.home.HomeFragment
import com.example.loginapp.presentation.login.LoginFragment
import com.example.loginapp.presentation.profile.ProfileFragment
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    var currentUserId: Int = -1

    @javax.inject.Inject
    lateinit var getUserUseCase: com.example.loginapp.domain.usecase.GetUserUseCase

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

        setDrawerEnabled(false)
    }

    fun setCurrentUser(userId: Int) {
        currentUserId = userId
        updateDrawerHeader(userId)
    }

    private fun updateDrawerHeader(userId: Int) {
        val navView: NavigationView = findViewById(R.id.nav_view)
        val headerView = navView.getHeaderView(0)
        val emailTextView: android.widget.TextView = headerView.findViewById(R.id.emailTextView)

        lifecycleScope.launch {
            getUserUseCase(userId).collect { result ->
                if (result is com.example.loginapp.common.Result.Success && result.data != null) {
                    emailTextView.text = result.data.email
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
                
                val homeFragment = com.example.loginapp.presentation.home.HomeFragment().apply {
                    arguments = Bundle().apply {
                        putInt("USER_ID", currentUserId)
                    }
                }
                
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, homeFragment)
                    .commit()
            }
            R.id.nav_account_summary -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, com.example.loginapp.presentation.summary.AccountSummaryFragment.newInstance(currentUserId))
                    .commit()
            }
            R.id.nav_profile -> {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
                if (currentFragment !is ProfileFragment) {
                    val profileFragment = ProfileFragment().apply {
                        arguments = Bundle().apply {
                            putInt("USER_ID", currentUserId)
                        }
                    }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, profileFragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
            R.id.nav_logout -> {
                com.google.android.material.dialog.MaterialAlertDialogBuilder(this, R.style.CustomDialog)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes") { _, _ ->
                        logout()
                    }
                    .setNegativeButton("No", null)
                    .show()
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
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, LoginFragment())
            .commit()
        setDrawerEnabled(false)
        currentUserId = -1
    }

    fun setDrawerEnabled(enabled: Boolean) {
        val lockMode = if (enabled) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        drawerLayout.setDrawerLockMode(lockMode)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, findViewById(R.id.toolbar),
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        toggle.isDrawerIndicatorEnabled = enabled
        toggle.syncState()
    }
}
