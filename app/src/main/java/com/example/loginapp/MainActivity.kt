package com.example.loginapp

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
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
        
        // Setup NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Setup AppBarConfiguration for top-level destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.profileFragment, R.id.accountSummaryFragment),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setNavigationItemSelectedListener(this)

        // Listen for destination changes to control drawer and toolbar visibility
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment, R.id.splashFragment -> {
                    supportActionBar?.hide()
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
                else -> {
                    supportActionBar?.show()
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }
            }
        }
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
                // Navigate to home using global action
                val bundle = Bundle().apply {
                    putInt("userId", currentUserId)
                    putString("category", "PERSONAL")
                }
                navController.navigate(R.id.action_global_homeFragment, bundle)
            }
            R.id.nav_account_summary -> {
                // Navigate using global action with arguments
                val bundle = Bundle().apply {
                    putInt("userId", currentUserId)
                }
                navController.navigate(R.id.action_global_accountSummaryFragment, bundle)
            }
            R.id.nav_profile -> {
                // Navigate using global action with arguments
                val bundle = Bundle().apply {
                    putInt("userId", currentUserId)
                }
                navController.navigate(R.id.action_global_profileFragment, bundle)
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

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun logout() {
        currentUserId = -1
        navController.navigate(R.id.action_global_loginFragment)
    }
}
