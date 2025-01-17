package com.dan.timelapse.filters

import com.dan.timelapse.images.ImageTools
import org.opencv.core.Mat

class DarkestPixelsFramesFilter(val size: Int, nextConsumer: FramesConsumer)
    : MultiFramesFilter(size, false, nextConsumer) {

    private val outputFrame = Mat()

    override fun stopFilter() {
        outputFrame.release()
        super.stopFilter()
    }

    override fun consume(index: Int, removedFrame: Mat, frames: List<Mat>) {
        ImageTools.mergeDarkestPixels(frames, outputFrame)
        next(index, outputFrame)
    }
}