package com.dan.timelapse

import org.opencv.core.Mat

interface FramesInput {
    val fps: Int
    val name: String
    val width: Int
    val height: Int

    fun forEachFrame(callback: (Mat)->Unit)
}