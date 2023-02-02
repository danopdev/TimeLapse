package com.dan.timelapse

import org.opencv.core.Mat

class RightInertiaFilter(private val size: Int, nextConsumer: FramesConsumer) : FramesFilter(nextConsumer) {
    private var _lastFrame: Mat? = null

    override fun consume(frame: Mat) {
        _lastFrame = frame
        next(frame)
    }

    override fun stop() {
        val lastFrame = _lastFrame
        if (null != lastFrame) {
            _lastFrame = null
            for (index in 1 until size) {
                next(lastFrame)
            }
        }
        super.stop()
    }
}