package com.dan.timelapse

import org.opencv.core.Core
import org.opencv.core.CvType.CV_16UC3
import org.opencv.core.CvType.CV_8UC3
import org.opencv.core.Mat

class TransitionFramesFilter(private val size: Int, nextConsumer: FramesConsumer)
    : MultiFramesFilter(2, true, nextConsumer) {
    private val tmpFrame1 = Mat()
    private val tmpFrame2 = Mat()
    private val outputFrame = Mat()

    override fun stopFilter() {
        tmpFrame1.release()
        tmpFrame2.release()
        outputFrame.release()
        super.stopFilter()
    }

    override fun consume(removedFrame: Mat?, lastFrame: Mat?, newFrame: Mat, allFrames: Array<Mat?>) {
        if (null != lastFrame) {
            for(step in 1 until size) {
                lastFrame.convertTo(tmpFrame1, CV_16UC3, (size - step).toDouble())
                newFrame.convertTo(tmpFrame2, CV_16UC3, step.toDouble())
                Core.add( tmpFrame1, tmpFrame2, tmpFrame1 )
                tmpFrame1.convertTo(outputFrame, CV_8UC3, 1.0 / size.toDouble())
                nextConsumer.consume(outputFrame)
            }
        }

        nextConsumer.consume(newFrame)
    }
}