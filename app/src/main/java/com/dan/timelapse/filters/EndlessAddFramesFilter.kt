package com.dan.timelapse.filters

import org.opencv.core.Core
import org.opencv.core.Mat

class EndlessAddFramesFilter(nextConsumer: FramesConsumer) : FramesFilter(nextConsumer) {
    private val sum = Mat()

    override fun consume(index: Int, frame: Mat) {
        if (sum.empty()) {
            sum.create(frame.size(), frame.type())
            frame.copyTo(sum)
        } else {
            Core.add(sum, frame, sum)
        }

        next(index, sum)
    }

    override fun stopFilter() {
        sum.release()
        super.stopFilter()
    }
}