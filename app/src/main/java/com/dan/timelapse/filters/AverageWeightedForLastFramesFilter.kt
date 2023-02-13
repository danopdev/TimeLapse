package com.dan.timelapse.filters

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat


class AverageWeightedForLastFramesFilter(size: Int, nextConsumer: FramesConsumer)
    : SumFramesFilter(size, nextConsumer) {
    private val extraSum = Mat()
    private val outputFrame = Mat()

    override fun stopFilter() {
        outputFrame.release()
        extraSum.release()
        super.stopFilter()
    }

    override fun consumeSum(index: Int, sum: Mat, frames: List<Mat>) {
        val extra = frames.size / 2
        Core.addWeighted( sum, 1.0, frames.last(), extra.toDouble(), 0.0, extraSum, sum.type() )
        extraSum.convertTo(outputFrame, CvType.CV_8UC3, 1.0 / (frames.size + extra))
        next(index, outputFrame)
    }
}
