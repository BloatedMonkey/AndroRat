package com.security.test.rat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * MainActivity - Entry point for Android application
 * 
 * This is a minimal Kotlin implementation for build compatibility
 */
class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSION_REQUEST_CODE = 1001
    }
    
    private lateinit var handler: Handler
    
    // Basic permissions for a simple app
    private val basicPermissions = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set a simple layout
        setContentView(R.layout.activity_main)
        
        // Initialize handler
        handler = Handler(Looper.getMainLooper())
        
        // Check if this is first run
        if (isFirstRun()) {
            handleFirstRun()
        } else {
            // Normal startup
            checkPermissionsAndStart()
        }
    }
    
    /**
     * Check if this is the first run of the app
     */
    private fun isFirstRun(): Boolean {
        // Simple first run check
        return true // Always treat as first run for now
    }
    
    /**
     * Handle first run setup
     */
    private fun handleFirstRun() {
        Log.d(TAG, "First run detected")
        
        // Request basic permissions
        requestPermissions()
    }
    
    /**
     * Check permissions and start app
     */
    private fun checkPermissionsAndStart() {
        if (checkPermissions()) {
            startApp()
        } else {
            requestPermissions()
        }
    }
    
    /**
     * Check if required permissions are granted
     */
    private fun checkPermissions(): Boolean {
        return basicPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Request required permissions
     */
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, basicPermissions, PERMISSION_REQUEST_CODE)
    }
    
    /**
     * Start the main app functionality
     */
    private fun startApp() {
        Log.d(TAG, "Starting app functionality")
        
        // Show a simple message
        Toast.makeText(this, "App started successfully!", Toast.LENGTH_SHORT).show()
        
        // For now, just log that we're running
        Log.i(TAG, "App is running with basic functionality")
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            
            if (allGranted) {
                startApp()
            } else {
                Toast.makeText(this, "Permissions required for app functionality", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}
