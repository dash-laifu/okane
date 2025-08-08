package com.dash_laifu.okane.shared.models

/**
 * Data class representing a notification item
 */
data class NotificationItem(
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long
)
