package com.dan.timelapse

import org.opencv.core.Core.addWeighted
import org.opencv.core.Mat

class TransitionFramesFilter(private val size: Int, nextConsumer: FramesConsumer)
    : MultiFramesFilter(2, true, nextConsumer) {
    private val outputFrame = Mat()

    override fun stopFilter() {
        outputFrame.release()
        super.stopFilter()
    }

    override fun consume(removedFrame: Mat?, lastFrame: Mat?, newFrame: Mat, allFrames: Array<Mat?>, nbOfValidFrames: Int) {
        if (null != lastFrame) {
            for(step in 1 until size) {
                val lastFrameWeight = (size - step).toDouble() / size
                addWeighted(lastFrame, lastFrameWeight, newFrame, 1.0 - lastFrameWeight, 0.0, outputFrame)
                nextConsumer.consume(outputFrame)
            }
        }

        nextConsumer.consume(newFrame)
    }
}