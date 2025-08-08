package com.dash_laifu.okane.shared.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

class AllowedAppsStorage(context: Context) {

    companion object {
        private const val TAG = "AllowedAppsStorage"
        private const val PREFS_NAME = "okane_allowed_apps"
        private const val KEY_ALLOWED_PACKAGES = "allowed_packages"
    }

    private val sharedPrefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun saveAllowedPackages(packageNames: Set<String>) = withContext(Dispatchers.IO) {
        try {
            val jsonArray = JSONArray()
            packageNames.forEach { packageName ->
                jsonArray.put(packageName)
            }
            
            sharedPrefs.edit()
                .putString(KEY_ALLOWED_PACKAGES, jsonArray.toString())
                .apply()
                
            Log.d(TAG, "Saved ${packageNames.size} allowed packages")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save allowed packages", e)
        }
    }

    suspend fun loadAllowedPackages(): Set<String> = withContext(Dispatchers.IO) {
        try {
            val jsonString = sharedPrefs.getString(KEY_ALLOWED_PACKAGES, null)
            if (jsonString.isNullOrEmpty()) {
                Log.d(TAG, "No saved allowed packages found")
                return@withContext emptySet()
            }

            val jsonArray = JSONArray(jsonString)
            val packages = mutableSetOf<String>()

            for (i in 0 until jsonArray.length()) {
                packages.add(jsonArray.getString(i))
            }

            Log.d(TAG, "Loaded ${packages.size} allowed packages")
            packages
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load allowed packages", e)
            emptySet()
        }
    }

    suspend fun clearAllowedPackages() = withContext(Dispatchers.IO) {
        try {
            sharedPrefs.edit().remove(KEY_ALLOWED_PACKAGES).apply()
            Log.d(TAG, "Cleared all allowed packages")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear allowed packages", e)
        }
    }
}
