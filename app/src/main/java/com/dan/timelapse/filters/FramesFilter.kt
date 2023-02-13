package com.dan.timelapse.filters

import org.opencv.core.Mat

abstract class FramesFilter(private val nextConsumer: FramesConsumer) : FramesConsumer {
    open fun startFilter() {}
    open fun stopFilter() {}

    fun next(index: Int, frame: Mat) {
        nextConsumer.consume(index, frame)
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