package com.dan.timelapse

import org.opencv.core.Mat

abstract class FramesFilter(private val nextConsumer: FramesConsumer) : FramesConsumer {
    open fun startFilter() {}
    open fun stopFilter() {}

    fun next(frame: Mat) {
        nextConsumer.consume(frame)
    }

    override fun start() {
        startFilter()
        nextConsumer.start()
    }

    override fun stop() {
        nextConsumer.stop()
        stopFilter()
    }
}