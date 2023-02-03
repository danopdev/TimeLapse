package com.dan.timelapse

import org.opencv.core.Core.addWeighted
import org.opencv.core.Mat

class TransitionFramesFilter(private val size: Int, nextConsumer: FramesConsumer)
    : FramesFilter(nextConsumer) {
    private val outputFrame = Mat()
    private var prevFrame = Mat()

    override fun stopFilter() {
        outputFrame.release()
        prevFrame.release()
        super.stopFilter()
    }

    override fun consume(frame: Mat) {
        if (!prevFrame.empty()) {
            for(step in 1 until size) {
                val lastFrameWeight = (size - step).toDouble() / size
                addWeighted(prevFrame, lastFrameWeight, frame, 1.0 - lastFrameWeight, 0.0, outputFrame)
                next(outputFrame)
            }
            prevFrame.release()
        }

        next(frame)
        prevFrame = frame.clone()
    }
}