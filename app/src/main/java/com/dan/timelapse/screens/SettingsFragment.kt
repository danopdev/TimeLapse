package com.dan.timelapse.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.dan.timelapse.MainActivity
import com.dan.timelapse.databinding.SettingsFragmentBinding


class SettingsFragment(activity: MainActivity) : AppFragment(activity) {

    companion object {
        private const val JPEG_QUALITY_BASE = 60

        fun show(activity: MainActivity) {
            activity.pushView("Settings", SettingsFragment( activity ))
        }
    }

    private lateinit var binding: SettingsFragmentBinding

    private fun update() {
        binding.switchH265.text = if (binding.switchH265.isChecked) "Encoder H.265 (HEVC)" else "Encoder H.264 (old)"
        binding.switchCrop.text = if (binding.switchCrop.isChecked) "Crop images" else "Don't crop (back borders)"
        binding.switch4K.text = if (binding.switch4K.isChecked) "Video 4K" else "Video 1080p"
    }

    override fun onBack(homeButton: Boolean) {
        settings.h265 = binding.switchH265.isChecked
        settings.crop = binding.switchCrop.isChecked
        settings.encode4K = binding.switch4K.isChecked

        settings.jpegQuality = JPEG_QUALITY_BASE + (100 - JPEG_QUALITY_BASE) * binding.seekBarJpegQuality.progress / binding.seekBarJpegQuality.max

        settings.saveProperties()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SettingsFragmentBinding.inflate( inflater )

        binding.switchH265.isChecked = settings.h265
        binding.switchCrop.isChecked = settings.crop
        binding.switch4K.isChecked = settings.encode4K

        binding.switchH265.setOnCheckedChangeListener { _, _ -> update()  }
        binding.switchCrop.setOnCheckedChangeListener { _, _ -> update()  }
        binding.switch4K.setOnCheckedChangeListener { _, _ -> update()  }

        val jpegQualityProgress = when {
            settings.jpegQuality >= 100 -> binding.seekBarJpegQuality.max
            settings.jpegQuality < JPEG_QUALITY_BASE -> 0
            else -> (settings.jpegQuality - JPEG_QUALITY_BASE) * binding.seekBarJpegQuality.max / (100 - JPEG_QUALITY_BASE)
        }

        binding.seekBarJpegQuality.progress = jpegQualityProgress
        binding.txtJpegQuality.text = settings.jpegQuality.toString()
        binding.seekBarJpegQuality.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                val jpegQuality = JPEG_QUALITY_BASE + (100 - JPEG_QUALITY_BASE) * progress / binding.seekBarJpegQuality.max
                binding.txtJpegQuality.text = jpegQuality.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        update()

        return binding.root
    }
}