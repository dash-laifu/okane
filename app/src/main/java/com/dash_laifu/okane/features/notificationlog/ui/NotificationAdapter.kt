package com.dash_laifu.okane.features.notificationlog.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dash_laifu.okane.R
import com.dash_laifu.okane.shared.models.NotificationItem
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    companion object {
        private const val TAG = "NotificationAdapter"
    }

    private var notifications: List<NotificationItem> = emptyList()
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun updateNotifications(newNotifications: List<NotificationItem>) {
        Log.d(TAG, "Updating adapter with ${newNotifications.size} notifications")
        notifications = newNotifications
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appNameTextView: TextView = itemView.findViewById(R.id.tvAppName)
        private val titleTextView: TextView = itemView.findViewById(R.id.tvTitle)
        private val textTextView: TextView = itemView.findViewById(R.id.tvText)
        private val timestampTextView: TextView = itemView.findViewById(R.id.tvTimestamp)

        fun bind(notification: NotificationItem) {
            appNameTextView.text = notification.appName
            titleTextView.text = notification.title
            textTextView.text = notification.text
            timestampTextView.text = dateFormat.format(Date(notification.timestamp))

            // Hide empty fields
            titleTextView.visibility = if (notification.title.isNotEmpty()) View.VISIBLE else View.GONE
            textTextView.visibility = if (notification.text.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }
}
