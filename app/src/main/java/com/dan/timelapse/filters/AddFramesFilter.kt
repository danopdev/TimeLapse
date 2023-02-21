package com.dan.timelapse.filters

import org.opencv.core.Core
import org.opencv.core.Mat

class AddFramesFilter(size: Int, nextConsumer: FramesConsumer)
    : MultiFramesFilter(size, true, nextConsumer) {

    private val sum = Mat()

    override fun stopFilter() {
        sum.release()
        super.stopFilter()
    }

    override fun consume(index: Int, removedFrame: Mat, frames: List<Mat>) {
        val firstFrame = frames.first()

        if (sum.empty()) sum.create(firstFrame.size(), firstFrame.type())

        firstFrame.copyTo(sum)
        for( frameIndex in 1 until frames.size) {
            Core.add(sum, frames[frameIndex], sum)
        }

        next(index, sum)
    }
}