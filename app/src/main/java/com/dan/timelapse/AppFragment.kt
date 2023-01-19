package com.dan.timelapse

import androidx.fragment.app.Fragment

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