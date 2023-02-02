package com.dan.timelapse

import org.opencv.core.Mat

class LeftInertiaFilter(private val size: Int, nextConsumer: FramesConsumer) : FramesFilter(nextConsumer) {
    private var firstFrame = true

    override fun consume(frame: Mat) {
        if (firstFrame) {
           firstFrame = false
           for (index in 1 until size) {
               next(frame)
           }
        }

        next(frame)
    }

    override fun stop() {
        firstFrame = true
        super.stop()
    }
}