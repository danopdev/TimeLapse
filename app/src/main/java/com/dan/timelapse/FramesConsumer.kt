package com.dan.timelapse

import org.opencv.core.Mat

interface FramesConsumer {
    fun start()
    fun stop()
    fun consume(index: Int, frame: Mat)
}