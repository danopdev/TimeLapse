package com.dan.timelapse.filters

import org.opencv.core.CvType.CV_8UC3
import org.opencv.core.Mat

class AverageFramesFilter(size: Int, nextConsumer: FramesConsumer)
    : SumFramesFilter(size, nextConsumer) {
    private val outputFrame = Mat()

    override fun stopFilter() {
        outputFrame.release()
        super.stopFilter()
    }

    override fun consumeSum(index: Int, sum: Mat, frames: List<Mat>) {
        sum.convertTo(outputFrame, CV_8UC3, 1.0 / frames.size)
        next(index, outputFrame)
    }
}