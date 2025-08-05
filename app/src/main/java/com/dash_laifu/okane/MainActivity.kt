package com.dash_laifu.okane

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
            // Add the notification log fragment
            if (supportFragmentManager.findFragmentById(R.id.main) == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main, NotificationLogFragment())
                    .commit()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check permission when user returns from settings
        if (isNotificationAccessGranted() && supportFragmentManager.findFragmentById(R.id.main) == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, NotificationLogFragment())
                .commit()
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

    private fun openNotificationAccessSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }
}