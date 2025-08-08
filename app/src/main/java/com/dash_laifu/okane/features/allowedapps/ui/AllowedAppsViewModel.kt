package com.dash_laifu.okane.features.allowedapps.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dash_laifu.okane.features.allowedapps.data.AllowedAppsRepository
import com.dash_laifu.okane.shared.models.AllowedApp
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AllowedAppsViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "AllowedAppsViewModel"
    }

    private val repository = AllowedAppsRepository.getInstance(application.applicationContext)

    val allowedApps: StateFlow<List<AllowedApp>> = repository.allowedApps

    init {
        Log.d(TAG, "AllowedAppsViewModel initialized")
    }

    fun toggleApp(packageName: String) {
        viewModelScope.launch {
            repository.toggleApp(packageName)
        }
    }

    fun clearAllowedApps() {
        viewModelScope.launch {
            repository.clearAllowedApps()
        }
    }
}
