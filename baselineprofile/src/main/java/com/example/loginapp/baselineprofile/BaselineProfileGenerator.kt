package com.example.loginapp.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(
            packageName = "com.example.loginapp",
            includeInStartupProfile = true
        ) {
            // Start the app
            pressHome()
            startActivityAndWait()

            // Wait for content to appear
            device.wait(Until.hasObject(By.text("Login")), 5000)

            // Perform critical user flows here
            // For example, if we had a way to login automatically or if we just want to optimize startup to login screen:
            
            // 1. Login Flow (if possible to automate without credentials or with mock credentials)
            // val usernameField = device.findObject(By.res("com.example.loginapp:id/etUsername"))
            // usernameField?.text = "testuser"
            // val passwordField = device.findObject(By.res("com.example.loginapp:id/etPassword"))
            // passwordField?.text = "password"
            // val loginButton = device.findObject(By.res("com.example.loginapp:id/btnLogin"))
            // loginButton?.click()
            
            // Wait for Home screen
            // device.wait(Until.hasObject(By.text("Money Tracker")), 5000)
        }
    }
}
