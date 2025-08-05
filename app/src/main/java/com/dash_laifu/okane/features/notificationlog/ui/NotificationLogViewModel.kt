package com.dash_laifu.okane.features.notificationlog.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dash_laifu.okane.features.notificationlog.data.NotificationRepository
import com.dash_laifu.okane.shared.models.NotificationItem
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationLogViewModel : ViewModel() {

    companion object {
        private const val TAG = "NotificationLogViewModel"
    }

    private val repository = NotificationRepository()

    val notifications: StateFlow<List<NotificationItem>> = repository.notifications

    init {
        Log.d(TAG, "NotificationLogViewModel initialized")
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
        }
    }
}
