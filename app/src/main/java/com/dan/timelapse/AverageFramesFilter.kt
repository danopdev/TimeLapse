package com.dan.timelapse

import org.opencv.core.Core
import org.opencv.core.CvType.CV_16UC3
import org.opencv.core.CvType.CV_8UC3
import org.opencv.core.Mat

class AverageFramesFilter(private val size: Int, nextConsumer: FramesConsumer) : FramesFilter(nextConsumer) {
    private val frames = mutableListOf<Mat>()
    private val outputFrame = Mat()
    private val sum = Mat()
    private var counter = 0

    override fun startFilter() {
        for (index in 1 .. size) {
            frames.add(Mat())
        }

        counter = 0

        super.startFilter()
    }

    override fun stopFilter() {
        sum.release()
        outputFrame.release()
        frames.clear()
        super.stopFilter()
    }

    override fun consume(frame: Mat) {
        val index = counter % size
        counter++

        val prevMat = frames[index]
        frames[index] = frame.clone()

        if (sum.empty()) {
            frame.convertTo(sum, CV_16UC3)
        } else {
            Core.add( sum, frame, sum, Mat(), CV_16UC3 )
        }

        if (!prevMat.empty()) {
            Core.subtract( sum, prevMat, sum, Mat(), CV_16UC3 )
        }

        if (counter < size) return

        sum.convertTo( outputFrame, CV_8UC3, 1.0 / size )
        nextConsumer.consume(outputFrame)
    }
}