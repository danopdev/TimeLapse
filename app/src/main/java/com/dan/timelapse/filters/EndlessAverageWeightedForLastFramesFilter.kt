package com.dan.timelapse.filters

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat


class EndlessAverageWeightedForLastFramesFilter(nextConsumer: FramesConsumer) : FramesFilter(nextConsumer) {
    private val sum = Mat()
    private val sumWeighted = Mat()
    private val outputFrame = Mat()
    private var size = 0

    override fun consume(index: Int, frame: Mat) {
        size++

        if (sum.empty()) {
            frame.convertTo(sum, CvType.CV_32SC3)
            next(index, frame)
            return
        }

        Core.add( sum, frame, sum, Mat(), CvType.CV_32SC3)

        val extra = size / 2
        Core.addWeighted( sum, 1.0, frame, extra.toDouble(), 0.0, sumWeighted, sum.type())

        sumWeighted.convertTo(outputFrame, CvType.CV_8UC3, 1.0 / (size + extra))
        next(index, outputFrame)
    }

    override fun stopFilter() {
        sum.release()
        sumWeighted.release()
        outputFrame.release()
        size = 0
        super.stopFilter()
    }
}