package com.dash_laifu.okane.shared.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dash_laifu.okane.features.notificationlog.data.NotificationRepository

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Boot receiver called with action: ${intent.action}")
        
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device boot completed, initializing notification repository")
            
            // Initialize the notification repository to re-register listeners
            // This will automatically re-connect to the NotificationListenerService
            try {
                NotificationRepository.getInstance(context.applicationContext)
                Log.d(TAG, "Notification repository initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize notification repository after boot", e)
            }
        }
    }
}
