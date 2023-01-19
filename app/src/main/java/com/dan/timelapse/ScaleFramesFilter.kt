package com.dan.timelapse

import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc.*

class ScaleFramesFilter(private val width: Int, private val height: Int, nextConsumer: FramesConsumer) : FramesFilter(nextConsumer) {
    private val scaledMat = Mat() //to avoid memory allocation at every frame if possible

    private fun scaleFrame(frame: Mat): Mat {
        //the size is already correct
        if (frame.width() == width && frame.height() == height) return frame

        //just crop is needed
        if ((frame.width() > width && frame.height() == height) ||
            (frame.width() == width && frame.height() > height) ) {
            return frame.submat(Rect((frame.width() - width) / 2, (frame.height() - height) / 2, width, height))
        }

        val scaleUp = frame.width() < width || frame.height() < height
        val scaleAlgorithm = if (scaleUp) INTER_LANCZOS4 else INTER_AREA

        var newWidth = width
        var newHeight = frame.height() * width / frame.width()

        if (newHeight < height) {
            newHeight = height
            newWidth = frame.width() * height / frame.height()
        }

        resize( frame, scaledMat, org.opencv.core.Size(newWidth.toDouble(), newHeight.toDouble()), 0.0, 0.0, scaleAlgorithm )
        return scaledMat.submat(Rect((scaledMat.width() - width) / 2, (scaledMat.height() - height) / 2, width, height))
    }

    override fun stopFilter() {
        scaledMat.release()
        super.stopFilter()
    }

    override fun consume(frame: Mat) {
        nextConsumer.consume(scaleFrame(frame))
    }
}