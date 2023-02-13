package com.dan.timelapse.filters

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat

class EndlessAverageWeightedForLightFramesFilter(nextConsumer: FramesConsumer): FramesFilter(nextConsumer) {

    companion object {
        private const val POWER = 6.0
    }

    private val power = Mat()
    private val sumWeighted = Mat()
    private val outputFrame = Mat()
    private var size = 0

    override fun stopFilter() {
        sumWeighted.release()
        power.release()
        outputFrame.release()
        size = 0
        super.stopFilter()
    }

    override fun consume(index: Int, frame: Mat) {
        size++

        frame.convertTo(power, CvType.CV_64FC3)
        Core.pow(power, POWER, power)

        if (sumWeighted.empty()) {
            sumWeighted.create(power.size(), power.type())
            power.copyTo(sumWeighted)
            next(index, frame)
            return
        }

        Core.add( sumWeighted, power, sumWeighted)
        sumWeighted.convertTo(power, sumWeighted.type(), 1.0 / size)
        Core.pow(power, 1.0 / POWER, power)
        power.convertTo(outputFrame, CvType.CV_8UC3)
        next(index, outputFrame)
    }
}