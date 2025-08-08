package com.dash_laifu.okane.features.allowedapps.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dash_laifu.okane.R
import kotlinx.coroutines.launch

class AllowedAppsFragment : Fragment() {

    private val viewModel: AllowedAppsViewModel by viewModels()
    private lateinit var adapter: SectionedAllowedAppsAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_allowed_apps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        setupClearButton(view)
        observeAllowedApps()
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewAllowedApps)
        adapter = SectionedAllowedAppsAdapter { packageName ->
            viewModel.toggleApp(packageName)
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AllowedAppsFragment.adapter
        }
    }

    private fun setupClearButton(view: View) {
        val clearButton = view.findViewById<Button>(R.id.btnClearAllowedApps)
        clearButton.setOnClickListener {
            viewModel.clearAllowedApps()
        }
    }

    private fun observeAllowedApps() {
        Log.d("AllowedAppsFragment", "Setting up allowed apps observer")
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allowedApps.collect { apps ->
                Log.d("AllowedAppsFragment", "Received ${apps.size} apps in UI")
                adapter.updateApps(apps)
            }
        }
    }
}
