package com.dan.timelapse.filters

import com.dan.timelapse.images.ImageTools
import org.opencv.core.Mat

class EndlessDarkestPixelsFramesFilter(nextConsumer: FramesConsumer): FramesFilter(nextConsumer) {
    private val outputFrame = Mat()

    override fun stopFilter() {
        outputFrame.release()
        super.stopFilter()
    }

    override fun consume(index: Int, frame: Mat) {
        if (outputFrame.empty()) {
            outputFrame.create(frame.size(), frame.type())
            frame.copyTo(outputFrame)
            next(index, frame)
            return
        }

        ImageTools.mergeDarkestPixels(listOf(outputFrame, frame), outputFrame)
        next(index, outputFrame)
    }
}