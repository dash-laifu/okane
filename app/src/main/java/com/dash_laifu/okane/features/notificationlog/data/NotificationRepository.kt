package com.dash_laifu.okane.features.notificationlog.data

import android.util.Log
import com.dash_laifu.okane.shared.models.NotificationItem
import com.dash_laifu.okane.shared.services.NotificationListenerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationRepository : NotificationListenerService.NotificationListener {

    companion object {
        private const val TAG = "NotificationRepository"
    }

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    init {
        Log.d(TAG, "NotificationRepository initialized, adding listener")
        NotificationListenerService.addListener(this)
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
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }
}
