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

class SectionedAllowedAppsAdapter(
    private val onToggleApp: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TAG = "SectionedAllowedAppsAdapter"
        private const val TYPE_HEADER = 0
        private const val TYPE_APP = 1
    }

    private var items: List<AdapterItem> = emptyList()

    sealed class AdapterItem {
        data class Header(val title: String, val count: Int) : AdapterItem()
        data class App(val allowedApp: AllowedApp) : AdapterItem()
    }

    fun updateApps(newApps: List<AllowedApp>) {
        Log.d(TAG, "Updating adapter with ${newApps.size} apps")
        
        val allowedApps = newApps.filter { it.isEnabled }
        val otherApps = newApps.filter { !it.isEnabled }
        
        val newItems = mutableListOf<AdapterItem>()
        
        // Add allowed apps section
        if (allowedApps.isNotEmpty()) {
            newItems.add(AdapterItem.Header("Allowed Apps", allowedApps.size))
            newItems.addAll(allowedApps.map { AdapterItem.App(it) })
        }
        
        // Add other apps section
        if (otherApps.isNotEmpty()) {
            newItems.add(AdapterItem.Header("Other Apps", otherApps.size))
            newItems.addAll(otherApps.map { AdapterItem.App(it) })
        }
        
        items = newItems
        notifyDataSetChanged()
        
        Log.d(TAG, "Updated with ${allowedApps.size} allowed apps and ${otherApps.size} other apps")
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is AdapterItem.Header -> TYPE_HEADER
            is AdapterItem.App -> TYPE_APP
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_section_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_allowed_app, parent, false)
                AllowedAppViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is AdapterItem.Header -> (holder as HeaderViewHolder).bind(item)
            is AdapterItem.App -> (holder as AllowedAppViewHolder).bind(item.allowedApp)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tvSectionTitle)

        fun bind(header: AdapterItem.Header) {
            titleTextView.text = "${header.title} (${header.count})"
        }
    }

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
