package com.dan.timelapse

import android.net.Uri
import org.opencv.core.Mat

interface FramesInput {
    companion object {
        fun fixName(original: String?): String {
            if (null == original) return "unknown"
            return original.split('.')[0]
        }
    }

    val fps: Int
    val name: String
    val width: Int
    val height: Int
    val size: Int
    val videoUri: Uri?

    fun forEachFrame(callback: (Int, Int, Mat)->Unit)
}