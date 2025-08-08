package com.dash_laifu.okane.shared.models

/**
 * Data class representing an installed app that can be toggled for notification listening
 */
data class AllowedApp(
    val packageName: String,
    val appName: String,
    val isEnabled: Boolean = false
)
