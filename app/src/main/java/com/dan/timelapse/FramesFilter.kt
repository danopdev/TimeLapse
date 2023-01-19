package com.dan.timelapse

abstract class FramesFilter(val nextConsumer: FramesConsumer) : FramesConsumer {
    open fun startFilter() {}
    open fun stopFilter() {}

    override fun start() {
        startFilter()
        nextConsumer.start()
    }

    override fun stop() {
        nextConsumer.stop()
        stopFilter()
    }
}