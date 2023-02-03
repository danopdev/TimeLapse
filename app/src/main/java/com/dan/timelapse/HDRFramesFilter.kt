package com.dan.timelapse

import org.opencv.core.Mat
import org.opencv.photo.Photo

class HDRFramesFilter(size: Int, nextConsumer: FramesConsumer)
    : MultiFramesFilter(size, false, nextConsumer) {
    private val hdrFrame = Mat()
    private val outputFrame = Mat()
    private val mergeMertens = Photo.createMergeMertens()

    override fun stopFilter() {
        hdrFrame.release()
        outputFrame.release()
        super.stopFilter()
    }

    override fun consume(removedFrame: Mat?, frames: List<Mat>) {
        mergeMertens.process(frames, hdrFrame)

        if (!hdrFrame.empty()) {
            hdrFrame.convertTo(outputFrame, frames[0].type(), 255.0)
            next(outputFrame)
        }
    }
}