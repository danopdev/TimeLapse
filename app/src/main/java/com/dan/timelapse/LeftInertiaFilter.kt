package com.dan.timelapse

import org.opencv.core.Mat

class LeftInertiaFilter(private val size: Int, nextConsumer: FramesConsumer) : FramesFilter(nextConsumer) {
    private var firstFrame = true

    override fun consume(index: Int, frame: Mat) {
        if (firstFrame) {
           firstFrame = false
           for (i in 1 until size) {
               next(index, frame)
           }
        }

        next(index, frame)
    }

    override fun stop() {
        firstFrame = true
        super.stop()
    }
}