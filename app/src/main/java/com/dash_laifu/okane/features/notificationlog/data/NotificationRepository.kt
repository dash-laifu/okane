package com.dash_laifu.okane.features.notificationlog.data

import android.content.Context
import android.util.Log
import com.dash_laifu.okane.shared.data.NotificationStorage
import com.dash_laifu.okane.shared.models.NotificationItem
import com.dash_laifu.okane.shared.services.NotificationListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationRepository private constructor() : NotificationListenerService.NotificationListener {

    companion object {
        private const val TAG = "NotificationRepository"
        
        @Volatile
        private var INSTANCE: NotificationRepository? = null
        
        @Volatile
        private var context: Context? = null
        
        fun getInstance(appContext: Context? = null): NotificationRepository {
            if (appContext != null) {
                context = appContext.applicationContext
            }
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationRepository().also { INSTANCE = it }
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val storage: NotificationStorage? by lazy {
        context?.let { NotificationStorage(it) }
    }

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    init {
        Log.d(TAG, "NotificationRepository initialized, adding listener")
        NotificationListenerService.addListener(this)
        loadSavedNotifications()
    }

    private fun loadSavedNotifications() {
        scope.launch {
            try {
                val savedNotifications = storage?.loadNotifications() ?: emptyList()
                _notifications.value = savedNotifications
                Log.d(TAG, "Loaded ${savedNotifications.size} saved notifications")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load saved notifications", e)
            }
        }
    }

    override fun onNotificationPosted(notification: NotificationListenerService.NotificationData) {
        Log.d(TAG, "Notification received in repository: ${notification.appName} - ${notification.title}")
        
        val notificationItem = NotificationItem(
            appName = notification.appName,
            title = notification.title,
            text = notification.text,
            timestamp = notification.timestamp
        )

        val currentList = _notifications.value.toMutableList()
        currentList.add(0, notificationItem) // Add to the beginning of the list
        _notifications.value = currentList
        
        Log.d(TAG, "Total notifications in list: ${currentList.size}")
        
        // Save to persistent storage
        scope.launch {
            try {
                storage?.saveNotifications(currentList)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save notifications", e)
            }
        }
    }

    fun clearNotifications() {
        Log.d(TAG, "Clearing all notifications")
        _notifications.value = emptyList()
        scope.launch {
            try {
                storage?.clearNotifications()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear saved notifications", e)
            }
        }
    }
}
