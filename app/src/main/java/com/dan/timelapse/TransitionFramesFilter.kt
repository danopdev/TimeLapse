package com.dan.timelapse

import org.opencv.core.Core
import org.opencv.core.CvType.CV_16UC3
import org.opencv.core.CvType.CV_8UC3
import org.opencv.core.Mat

class TransitionFramesFilter(private val size: Int, nextConsumer: FramesConsumer) : FramesFilter(nextConsumer) {
    private var prevFrame = Mat()
    private val tmpFrame1 = Mat()
    private val tmpFrame2 = Mat()
    private val outputFrame = Mat()

    override fun stopFilter() {
        prevFrame.release()
        tmpFrame1.release()
        tmpFrame2.release()
        outputFrame.release()
        super.stopFilter()
    }

    override fun consume(frame: Mat) {
        if (!prevFrame.empty()) {
            for(step in 1 until size) {
                prevFrame.convertTo(tmpFrame1, CV_16UC3, (size - step).toDouble())
                frame.convertTo(tmpFrame2, CV_16UC3, step.toDouble())
                Core.add( tmpFrame1, tmpFrame2, tmpFrame1 )
                tmpFrame1.convertTo(outputFrame, CV_8UC3, 1.0 / size.toDouble())
                nextConsumer.consume(outputFrame)
            }
        }

        prevFrame = frame.clone()
        nextConsumer.consume(frame)
    }
}