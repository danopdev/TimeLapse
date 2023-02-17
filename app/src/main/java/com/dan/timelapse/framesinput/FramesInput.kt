package com.dan.timelapse.framesinput

import android.net.Uri
import org.opencv.core.Mat

abstract class FramesInput {
    companion object {
        fun fixName(original: String?): String {
            if (null == original) return "unknown"
            return original.split('.')[0]
        }
    }

    abstract val fps: Int
    abstract val name: String
    abstract val width: Int
    abstract val height: Int
    abstract val size: Int
    abstract val videoUri: Uri?

    abstract fun forEachFrame(callback: (Int, Int, Mat)->Boolean)

    fun firstFrame(): Mat {
        var firstFrame = Mat()
        forEachFrame { _, _, frame ->
            firstFrame = frame.clone()
            false
        }
        return firstFrame
    }
}