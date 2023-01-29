package com.dan.timelapse

import org.opencv.core.Core
import org.opencv.core.CvType.CV_16UC3
import org.opencv.core.CvType.CV_8UC3
import org.opencv.core.Mat

class AverageFramesFilter(private val size: Int, nextConsumer: FramesConsumer)
    : MultiFramesFilter(size, true, nextConsumer) {
    private val outputFrame = Mat()
    private val sum = Mat()

    override fun stopFilter() {
        sum.release()
        outputFrame.release()
        super.stopFilter()
    }

    override fun consume(removedFrame: Mat?, lastFrame: Mat?, newFrame: Mat, allFrames: Array<Mat?>) {
        if (sum.empty()) {
            newFrame.convertTo(sum, CV_16UC3)
        } else {
            Core.add( sum, newFrame, sum, Mat(), CV_16UC3 )
        }

        if (null != removedFrame) {
            Core.subtract( sum, removedFrame, sum, Mat(), CV_16UC3 )
        }

        if (null == lastFrame) return

        sum.convertTo( outputFrame, CV_8UC3, 1.0 / size )
        nextConsumer.consume(outputFrame)
    }
}