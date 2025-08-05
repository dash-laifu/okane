package com.dash_laifu.okane.shared.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.dash_laifu.okane.shared.models.NotificationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class NotificationStorage(context: Context) {

    companion object {
        private const val TAG = "NotificationStorage"
        private const val PREFS_NAME = "okane_notifications"
        private const val KEY_NOTIFICATIONS = "notifications"
        private const val MAX_STORED_NOTIFICATIONS = 1000
    }

    private val sharedPrefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun saveNotifications(notifications: List<NotificationItem>) = withContext(Dispatchers.IO) {
        try {
            val jsonArray = JSONArray()
            val limitedNotifications = notifications.take(MAX_STORED_NOTIFICATIONS)
            
            limitedNotifications.forEach { notification ->
                val jsonObject = JSONObject().apply {
                    put("appName", notification.appName)
                    put("title", notification.title)
                    put("text", notification.text)
                    put("timestamp", notification.timestamp)
                }
                jsonArray.put(jsonObject)
            }
            
            sharedPrefs.edit()
                .putString(KEY_NOTIFICATIONS, jsonArray.toString())
                .apply()
                
            Log.d(TAG, "Saved ${limitedNotifications.size} notifications")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save notifications", e)
        }
    }

    suspend fun loadNotifications(): List<NotificationItem> = withContext(Dispatchers.IO) {
        try {
            val jsonString = sharedPrefs.getString(KEY_NOTIFICATIONS, null)
            if (jsonString.isNullOrEmpty()) {
                Log.d(TAG, "No saved notifications found")
                return@withContext emptyList()
            }

            val jsonArray = JSONArray(jsonString)
            val notifications = mutableListOf<NotificationItem>()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val notification = NotificationItem(
                    appName = jsonObject.getString("appName"),
                    title = jsonObject.getString("title"),
                    text = jsonObject.getString("text"),
                    timestamp = jsonObject.getLong("timestamp")
                )
                notifications.add(notification)
            }

            Log.d(TAG, "Loaded ${notifications.size} notifications")
            notifications
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load notifications", e)
            emptyList()
        }
    }

    suspend fun clearNotifications() = withContext(Dispatchers.IO) {
        try {
            sharedPrefs.edit().remove(KEY_NOTIFICATIONS).apply()
            Log.d(TAG, "Cleared all saved notifications")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear notifications", e)
        }
    }
}
