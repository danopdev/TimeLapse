package com.dan.timelapse.filters

import org.opencv.core.Mat

interface FramesConsumer {
    fun start()
    fun stop()
    fun consume(index: Int, frame: Mat)
}