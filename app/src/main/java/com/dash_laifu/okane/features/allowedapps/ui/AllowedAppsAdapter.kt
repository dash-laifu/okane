package com.dash_laifu.okane.features.allowedapps.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dash_laifu.okane.R
import com.dash_laifu.okane.shared.models.AllowedApp

class AllowedAppsAdapter(
    private val onToggleApp: (String) -> Unit
) : RecyclerView.Adapter<AllowedAppsAdapter.AllowedAppViewHolder>() {

    companion object {
        private const val TAG = "AllowedAppsAdapter"
    }

    private var apps: List<AllowedApp> = emptyList()

    fun updateApps(newApps: List<AllowedApp>) {
        Log.d(TAG, "Updating adapter with ${newApps.size} apps")
        apps = newApps
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllowedAppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_allowed_app, parent, false)
        return AllowedAppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AllowedAppViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    override fun getItemCount(): Int = apps.size

    inner class AllowedAppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appNameTextView: TextView = itemView.findViewById(R.id.tvAppName)
        private val packageNameTextView: TextView = itemView.findViewById(R.id.tvPackageName)
        private val enabledSwitch: Switch = itemView.findViewById(R.id.switchEnabled)

        fun bind(app: AllowedApp) {
            appNameTextView.text = app.appName
            packageNameTextView.text = app.packageName
            
            // Remove listener temporarily to avoid triggering during setup
            enabledSwitch.setOnCheckedChangeListener(null)
            enabledSwitch.isChecked = app.isEnabled
            
            // Set listener
            enabledSwitch.setOnCheckedChangeListener { _, _ ->
                onToggleApp(app.packageName)
            }
        }
    }
}
