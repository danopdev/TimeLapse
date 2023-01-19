package com.dan.timelapse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dan.timelapse.databinding.SettingsFragmentBinding


class SettingsFragment(activity: MainActivity ) : AppFragment(activity) {

    companion object {
        fun show(activity: MainActivity ) {
            activity.pushView("Settings", SettingsFragment( activity ))
        }
    }

    private lateinit var binding: SettingsFragmentBinding

    override fun onBack(homeButton: Boolean) {
        settings.encoder = binding.videoEncoder.selectedItemPosition
        settings.saveProperties()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SettingsFragmentBinding.inflate( inflater )

        binding.videoEncoder.setSelection(settings.encoder)

        return binding.root
    }
}