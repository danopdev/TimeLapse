package com.dan.timelapse

import org.opencv.core.Mat

class VideoWriter(
    private val path: String,
    private val fps: Int,
    private val width: Int,
    private val height: Int,
    private val h265: Boolean)
    : FramesConsumer {

    private var encoder: VideoEncoder? = null

    override fun start() {
        encoder = VideoEncoder.create(path, fps, width, height, 0, h265)
    }

    override fun stop() {
        encoder?.let {
            it.release()
            encoder = null
        }
    }

    override fun consume(frame: Mat) {
        encoder?.write(frame)
    }
}