package com.dash_laifu.okane

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dash_laifu.okane.features.notificationlog.data.NotificationRepository
import com.dash_laifu.okane.features.notificationlog.ui.NotificationLogFragment

class MainActivity : AppCompatActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            checkNotificationAccess()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check permissions
        checkPermissions()
    }

    private fun checkPermissions() {
        // Check POST_NOTIFICATIONS permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        
        checkNotificationAccess()
    }

    private fun checkNotificationAccess() {
        // Check notification listener permission
        if (!isNotificationAccessGranted()) {
            showNotificationAccessDialog()
        } else {
            // Check battery optimization
            checkBatteryOptimization()
        }
    }

    private fun checkBatteryOptimization() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                showBatteryOptimizationDialog()
                return
            }
        }
        
        // Initialize repository and show fragment
        initializeApp()
    }

    private fun initializeApp() {
        // Initialize the repository singleton to ensure the service listener is registered
        NotificationRepository.getInstance(applicationContext)
        
        // Add the notification log fragment
        if (supportFragmentManager.findFragmentById(R.id.main) == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, NotificationLogFragment())
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check permissions when user returns from settings
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val batteryOptimized = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            !powerManager.isIgnoringBatteryOptimizations(packageName)
        } else false
        
        if (isNotificationAccessGranted() && !batteryOptimized && 
            supportFragmentManager.findFragmentById(R.id.main) == null) {
            initializeApp()
        }
    }

    private fun isNotificationAccessGranted(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        val isGranted = !TextUtils.isEmpty(enabledListeners) && enabledListeners.contains(packageName)
        Log.d("MainActivity", "Notification access granted: $isGranted")
        Log.d("MainActivity", "Enabled listeners: $enabledListeners")
        return isGranted
    }

    private fun showNotificationAccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Notification Access Required")
            .setMessage("To log notifications, this app needs notification access permission. Please enable it in the settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                openNotificationAccessSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showBatteryOptimizationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Battery Optimization")
            .setMessage("To ensure notifications are captured even when the app is not running, please disable battery optimization for this app.")
            .setPositiveButton("Open Settings") { _, _ ->
                openBatteryOptimizationSettings()
            }
            .setNegativeButton("Skip") { _, _ ->
                initializeApp()
            }
            .setCancelable(false)
            .show()
    }

    private fun openNotificationAccessSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }

    private fun openBatteryOptimizationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent().apply {
                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                data = Uri.parse("package:$packageName")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to open battery optimization settings", e)
                // Fallback to general battery optimization settings
                val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                startActivity(fallbackIntent)
            }
        }
    }
}