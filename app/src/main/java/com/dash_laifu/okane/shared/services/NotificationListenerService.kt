package com.dash_laifu.okane.shared.services

import android.content.Context
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationListener"
        private var instance: NotificationListenerService? = null
        private val listeners = mutableListOf<NotificationListener>()

        fun addListener(listener: NotificationListener) {
            listeners.add(listener)
        }

        fun removeListener(listener: NotificationListener) {
            listeners.remove(listener)
        }
    }

    private var wakeLock: PowerManager.WakeLock? = null

    interface NotificationListener {
        fun onNotificationPosted(notification: NotificationData)
    }

    data class NotificationData(
        val appName: String,
        val title: String,
        val text: String,
        val timestamp: Long
    )

    override fun onCreate() {
        super.onCreate()
        // Acquire wake lock to ensure service continues running when screen is off
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Okane::NotificationListener"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.release()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        Log.d(TAG, "NotificationListenerService connected")
        Log.d(TAG, "Number of listeners registered: ${listeners.size}")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        Log.d(TAG, "NotificationListenerService disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.d(TAG, "onNotificationPosted called for package: ${sbn.packageName}")
        
        // Temporarily acquire wake lock to process notification
        wakeLock?.acquire(5000) // 5 second timeout
        
        val notification = sbn.notification
        val extras = notification.extras

        // Extract notification data
        val appName = getApplicationLabel(sbn.packageName)
        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val timestamp = sbn.postTime

        Log.d(TAG, "Notification details - App: $appName, Title: '$title', Text: '$text'")

        // Skip empty notifications and our own notifications
        if ((title.isEmpty() && text.isEmpty()) || sbn.packageName == packageName) {
            Log.d(TAG, "Skipping notification - empty or from own app")
            wakeLock?.release()
            return
        }

        val notificationData = NotificationData(
            appName = appName,
            title = title,
            text = text,
            timestamp = timestamp
        )

        Log.d(TAG, "Processing notification: $appName - $title")
        Log.d(TAG, "Number of listeners to notify: ${listeners.size}")

        // Notify all listeners
        listeners.forEach { listener ->
            Log.d(TAG, "Notifying listener: ${listener.javaClass.simpleName}")
            listener.onNotificationPosted(notificationData)
        }
        
        wakeLock?.release()
    }

    private fun getApplicationLabel(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
}
