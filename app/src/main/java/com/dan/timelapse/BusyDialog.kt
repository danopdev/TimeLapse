package com.dan.timelapse

import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.dan.timelapse.databinding.BusyDialogBinding

class BusyDialog( private var message: String): DialogFragment() {

    companion object {
        private const val FRAGMENT_TAG = "busy"
        private var currentDialog: BusyDialog? = null
        private lateinit var activity: MainActivity

        fun create(activity_: MainActivity) {
            activity = activity_
        }

        private fun runSafe( callback: ()->Unit ) {
            if (Looper.getMainLooper().isCurrentThread) {
                callback()
            } else {
                activity.runOnUiThread {
                    callback()
                }
            }
        }

        fun show(message: String) {
            runSafe {
                if (null == currentDialog) {
                    val dialog = BusyDialog(message)
                    dialog.isCancelable = false
                    dialog.show(activity.supportFragmentManager, FRAGMENT_TAG)
                    currentDialog = dialog
                } else {
                    currentDialog?.binding?.textBusyMessage?.text = message
                }
            }
        }

        fun updateProgress(progress: Int, total: Int) {
            if (total <= 0 || progress < 0 || progress > total) return

            runSafe {
                currentDialog?.binding?.progressBar?.apply {
                    this.max = total
                    this.progress = progress
                    this.isIndeterminate = false
                }
            }
        }

        fun dismiss() {
            runSafe {
                currentDialog?.dismiss()
                currentDialog = null
            }
        }
    }

    private var binding: BusyDialogBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = BusyDialogBinding.inflate( inflater )
        binding.textBusyMessage.text = message
        this.binding = binding
        return binding.root
    }
}
