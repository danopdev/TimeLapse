package com.dan.timelapse

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat


class AverageWeightedForLightFramesFilter(private val size: Int, nextConsumer: FramesConsumer)
    : MultiFramesFilter(size, true, nextConsumer) {

    companion object {
        private const val POWER = 6.0
    }

    private val power = Mat()
    private val sumWeighted = Mat()
    private val outputFrame = Mat()

    override fun stopFilter() {
        sumWeighted.release()
        power.release()
        outputFrame.release()
        super.stopFilter()
    }

    override fun consume(index: Int, removedFrame: Mat, frames: List<Mat>) {
        frames.last().convertTo(power, CvType.CV_64FC3)
        Core.pow(power, POWER, power)

        if (sumWeighted.empty()) {
            sumWeighted.create(power.size(), power.type())
            power.copyTo(sumWeighted)
        } else {
            Core.add( sumWeighted, power, sumWeighted)
        }

        if (!removedFrame.empty()) {
            removedFrame.convertTo(power, CvType.CV_64FC3)
            Core.pow(power, POWER, power)
            Core.subtract( sumWeighted, power, sumWeighted)
        }

        if (frames.size >= size) {
            sumWeighted.convertTo(power, sumWeighted.type(), 1.0 / frames.size)
            Core.pow(power, 1.0 / POWER, power)
            power.convertTo(outputFrame, CvType.CV_8UC3)
            next(index, outputFrame)
        }
    }
}