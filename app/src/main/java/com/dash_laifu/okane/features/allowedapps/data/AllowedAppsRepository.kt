package com.dash_laifu.okane.features.allowedapps.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.dash_laifu.okane.shared.data.AllowedAppsStorage
import com.dash_laifu.okane.shared.models.AllowedApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AllowedAppsRepository private constructor() {

    companion object {
        private const val TAG = "AllowedAppsRepository"
        
        @Volatile
        private var INSTANCE: AllowedAppsRepository? = null
        
        @Volatile
        private var context: Context? = null
        
        fun getInstance(appContext: Context? = null): AllowedAppsRepository {
            if (appContext != null) {
                context = appContext.applicationContext
            }
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AllowedAppsRepository().also { INSTANCE = it }
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val storage: AllowedAppsStorage? by lazy {
        context?.let { AllowedAppsStorage(it) }
    }

    private val _allowedApps = MutableStateFlow<List<AllowedApp>>(emptyList())
    val allowedApps: StateFlow<List<AllowedApp>> = _allowedApps.asStateFlow()

    private val _allowedPackages = MutableStateFlow<Set<String>>(emptySet())
    val allowedPackages: StateFlow<Set<String>> = _allowedPackages.asStateFlow()

    init {
        Log.d(TAG, "AllowedAppsRepository initialized")
        loadAllowedPackages()
        loadInstalledApps()
    }

    private fun loadAllowedPackages() {
        scope.launch {
            try {
                val savedPackages = storage?.loadAllowedPackages() ?: emptySet()
                _allowedPackages.value = savedPackages
                Log.d(TAG, "Loaded ${savedPackages.size} allowed packages")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load allowed packages", e)
            }
        }
    }

    private fun loadInstalledApps() {
        scope.launch(Dispatchers.IO) {
            try {
                val packageManager = context?.packageManager ?: return@launch
                val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                
                val allowedPackageNames = _allowedPackages.value
                val appsList = installedApps
                    .filter { appInfo ->
                        // Include apps that can potentially send notifications
                        // This includes both user apps and some system apps
                        val isUserApp = appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                        val isUpdatedSystemApp = appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
                        val hasLaunchIntent = packageManager.getLaunchIntentForPackage(appInfo.packageName) != null
                        
                        // Include user apps, updated system apps, or apps with launch intent
                        isUserApp || isUpdatedSystemApp || hasLaunchIntent
                    }
                    .map { appInfo ->
                        val appName = try {
                            packageManager.getApplicationLabel(appInfo).toString()
                        } catch (e: Exception) {
                            appInfo.packageName
                        }
                        
                        AllowedApp(
                            packageName = appInfo.packageName,
                            appName = appName,
                            isEnabled = allowedPackageNames.contains(appInfo.packageName)
                        )
                    }
                    .sortedWith(compareBy<AllowedApp> { !it.isEnabled }.thenBy { it.appName.lowercase() })

                _allowedApps.value = appsList
                Log.d(TAG, "Loaded ${appsList.size} installed apps (${appsList.count { it.isEnabled }} enabled)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load installed apps", e)
            }
        }
    }

    fun toggleApp(packageName: String) {
        scope.launch {
            try {
                val currentPackages = _allowedPackages.value.toMutableSet()
                
                if (currentPackages.contains(packageName)) {
                    currentPackages.remove(packageName)
                } else {
                    currentPackages.add(packageName)
                }
                
                _allowedPackages.value = currentPackages
                
                // Update the apps list and re-sort (enabled first)
                val updatedApps = _allowedApps.value.map { app ->
                    if (app.packageName == packageName) {
                        app.copy(isEnabled = currentPackages.contains(packageName))
                    } else {
                        app
                    }
                }.sortedWith(compareBy<AllowedApp> { !it.isEnabled }.thenBy { it.appName.lowercase() })
                
                _allowedApps.value = updatedApps
                
                // Save to storage
                storage?.saveAllowedPackages(currentPackages)
                
                Log.d(TAG, "Toggled app: $packageName, now enabled: ${currentPackages.contains(packageName)}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle app", e)
            }
        }
    }

    fun isPackageAllowed(packageName: String): Boolean {
        // If no apps are configured, allow all (default behavior)
        if (_allowedPackages.value.isEmpty()) {
            return true
        }
        return _allowedPackages.value.contains(packageName)
    }

    fun clearAllowedApps() {
        scope.launch {
            try {
                _allowedPackages.value = emptySet()
                _allowedApps.value = _allowedApps.value.map { it.copy(isEnabled = false) }
                storage?.clearAllowedPackages()
                Log.d(TAG, "Cleared all allowed apps")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear allowed apps", e)
            }
        }
    }
}
