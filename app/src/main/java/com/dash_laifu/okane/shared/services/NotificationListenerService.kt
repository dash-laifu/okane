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
        
        // Ensure repository is initialized when service connects
        // This is important for when the service starts independently of the main app
        try {
            val repositoryClass = Class.forName("com.dash_laifu.okane.features.notificationlog.data.NotificationRepository")
            val getInstanceMethod = repositoryClass.getMethod("getInstance", Class.forName("android.content.Context"))
            getInstanceMethod.invoke(null, applicationContext)
            Log.d(TAG, "NotificationRepository initialized from service")
            
            // Also initialize AllowedAppsRepository
            val allowedAppsRepositoryClass = Class.forName("com.dash_laifu.okane.features.allowedapps.data.AllowedAppsRepository")
            val getAllowedAppsInstanceMethod = allowedAppsRepositoryClass.getMethod("getInstance", Class.forName("android.content.Context"))
            getAllowedAppsInstanceMethod.invoke(null, applicationContext)
            Log.d(TAG, "AllowedAppsRepository initialized from service")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize repositories from service", e)
        }
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
        
        // Check if this app is allowed
        val isAllowed = isAppAllowed(sbn.packageName)
        if (!isAllowed) {
            Log.d(TAG, "Skipping notification from ${sbn.packageName} - not in allowed list")
            wakeLock?.release()
            return
        }
        
        val notification = sbn.notification
        val extras = notification.extras

        // Extract notification data
        val appName = getApplicationLabel(sbn.packageName)
        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val timestamp = sbn.postTime

        Log.d(TAG, "Notification details - App: $appName, Title: '$title', Text: '$text'")

        // Skip empty notifications
        if (title.isEmpty() && text.isEmpty()) {
            Log.d(TAG, "Skipping notification - empty content")
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

    private fun isAppAllowed(packageName: String): Boolean {
        return try {
            // Use reflection to access the AllowedAppsRepository
            val repositoryClass = Class.forName("com.dash_laifu.okane.features.allowedapps.data.AllowedAppsRepository")
            val getInstanceMethod = repositoryClass.getMethod("getInstance", Class.forName("android.content.Context"))
            val repositoryInstance = getInstanceMethod.invoke(null, applicationContext)
            val isPackageAllowedMethod = repositoryClass.getMethod("isPackageAllowed", String::class.java)
            
            // Get the current allowed packages for debugging
            val allowedPackagesField = repositoryClass.getDeclaredField("allowedPackages")
            allowedPackagesField.isAccessible = true
            val stateFlow = allowedPackagesField.get(repositoryInstance)
            val valueMethod = stateFlow.javaClass.getMethod("getValue")
            val allowedPackages = valueMethod.invoke(stateFlow) as Set<*>
            
            Log.d(TAG, "Current allowed packages: $allowedPackages")
            Log.d(TAG, "Checking package: $packageName")
            
            // Check if this package is allowed using the dedicated method
            val isAllowed = isPackageAllowedMethod.invoke(repositoryInstance, packageName) as Boolean
            Log.d(TAG, "Package $packageName allowed: $isAllowed")
            return isAllowed
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking allowed apps, defaulting to allow", e)
            true // Default to allowing notifications if there's an error
        }
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
