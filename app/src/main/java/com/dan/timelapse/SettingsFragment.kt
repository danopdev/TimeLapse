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
        settings.h265 = binding.switchH265.isChecked
        settings.crop = binding.switchCrop.isChecked
        settings.encode4K = binding.switch4K.isChecked

        settings.saveProperties()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SettingsFragmentBinding.inflate( inflater )

        binding.switchH265.isChecked = settings.h265
        binding.switchCrop.isChecked = settings.crop
        binding.switch4K.isChecked = settings.encode4K

        return binding.root
    }
}