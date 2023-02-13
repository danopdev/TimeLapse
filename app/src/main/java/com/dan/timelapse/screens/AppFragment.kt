package com.dan.timelapse.screens

import androidx.fragment.app.Fragment
import com.dan.timelapse.MainActivity

open class AppFragment(val activity: MainActivity) : Fragment() {

    val settings = activity.settings

    fun runOnUiThread(action: ()->Unit) {
        activity.runOnUiThread(action)
    }

    open fun onBack(homeButton: Boolean) {
    }

    fun showToast(message: String) {
        runOnUiThread {
            activity.showToast(message)
        }
    }
}