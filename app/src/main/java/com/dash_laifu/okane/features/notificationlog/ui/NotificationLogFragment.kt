package com.dash_laifu.okane.features.notificationlog.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dash_laifu.okane.R
import kotlinx.coroutines.launch

class NotificationLogFragment : Fragment() {

    private val viewModel: NotificationLogViewModel by viewModels()
    private lateinit var adapter: NotificationAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification_log, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        setupTestButton(view)
        observeNotifications()
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        adapter = NotificationAdapter()
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@NotificationLogFragment.adapter
        }
    }

    private fun setupTestButton(view: View) {
        val testButton = view.findViewById<Button>(R.id.btnTestNotification)
        val clearButton = view.findViewById<Button>(R.id.btnClearNotifications)
        
        testButton.setOnClickListener {
            sendTestNotification()
        }
        
        clearButton.setOnClickListener {
            viewModel.clearNotifications()
        }
    }

    private fun sendTestNotification() {
        Log.d("NotificationLogFragment", "Sending test notification")
        val context = requireContext()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "test_channel",
                "Test Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create and send test notification with unique ID and content
        val testId = System.currentTimeMillis().toInt()
        val notification = NotificationCompat.Builder(context, "test_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Test Notification #$testId")
            .setContentText("This is a test notification from Okane app at ${System.currentTimeMillis()}")
            .setAutoCancel(true)
            .build()

        notificationManager.notify(testId, notification)
        Log.d("NotificationLogFragment", "Test notification sent with ID: $testId")
    }

    private fun observeNotifications() {
        Log.d("NotificationLogFragment", "Setting up notifications observer")
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notifications.collect { notifications ->
                Log.d("NotificationLogFragment", "Received ${notifications.size} notifications in UI")
                adapter.updateNotifications(notifications)
            }
        }
    }
}
