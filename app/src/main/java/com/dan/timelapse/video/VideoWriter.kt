package com.dan.timelapse.video

import com.dan.timelapse.filters.FramesConsumer
import org.opencv.core.Mat

class VideoWriter(
    private val path: String,
    private val fps: Int,
    private val h265: Boolean)
    : FramesConsumer {

    private var encoder: VideoEncoder? = null

    override fun start() {

    }

    override fun stop() {
        encoder?.let {
            it.release()
            encoder = null
        }
    }

    override fun consume(index: Int, frame: Mat) {
        if (null == encoder) {
            encoder = VideoEncoder.create(path, fps, frame.width(), frame.height(), 0, h265)
        }

        encoder?.write(frame)
    }
}