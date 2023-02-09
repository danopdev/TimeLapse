package com.dan.timelapse

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
        var counter = frames.size + 1
        Core.add( sum, frames.last(), extraSum, Mat(), sum.type() )

        for(i in 1 until frames.size / 2) {
            Core.add( extraSum, frames.last(), extraSum, Mat(), sum.type() )
            counter++
        }

        extraSum.convertTo(outputFrame, CvType.CV_8UC3, 1.0 / counter)
        next(index, outputFrame)
    }
}
