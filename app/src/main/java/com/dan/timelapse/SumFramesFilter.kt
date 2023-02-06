package com.dan.timelapse

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat


abstract class SumFramesFilter(private val size: Int, nextConsumer: FramesConsumer)
    : MultiFramesFilter(size, true, nextConsumer) {
    private val sum = Mat()

    override fun stopFilter() {
        sum.release()
        super.stopFilter()
    }

    abstract fun consumeSum(index: Int, sum: Mat, frames: List<Mat>)

    override fun consume(index: Int, removedFrame: Mat, frames: List<Mat>) {
        val newFrame = frames.last()

        if (sum.empty()) {
            newFrame.convertTo(sum, CvType.CV_16UC3)
        } else {
            Core.add( sum, newFrame, sum, Mat(), CvType.CV_16UC3)
        }

        if (!removedFrame.empty()) {
            Core.subtract( sum, removedFrame, sum, Mat(), CvType.CV_16UC3)
        }

        if (frames.size >= size) {
            consumeSum(index, sum, frames)
        }
    }
}