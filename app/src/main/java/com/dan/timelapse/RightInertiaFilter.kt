package com.dan.timelapse

import org.opencv.core.Mat

class RightInertiaFilter(private val size: Int, nextConsumer: FramesConsumer) : FramesFilter(nextConsumer) {
    private var _lastFrame: Mat? = null
    private var _lastIndex: Int = 0

    override fun consume(index: Int, frame: Mat) {
        _lastFrame = frame
        _lastIndex = index
        next(index, frame)
    }

    override fun stop() {
        val lastFrame = _lastFrame
        if (null != lastFrame) {
            _lastFrame = null
            for (index in 1 until size) {
                next(_lastIndex, lastFrame)
            }
        }
        super.stop()
    }
}