package com.dan.timelapse

import org.opencv.core.Mat
import org.opencv.photo.Photo

class HDRFramesFilter(private val size: Int, nextConsumer: FramesConsumer)
    : MultiFramesFilter(size, true, nextConsumer) {
    private val hdrFrame = Mat()
    private val outputFrame = Mat()
    private val mergeMertens = Photo.createMergeMertensForPipeline()

    override fun stopFilter() {
        hdrFrame.release()
        outputFrame.release()
        mergeMertens.release()
        super.stopFilter()
    }

    override fun consume(index: Int, removedFrame: Mat, frames: List<Mat>) {
        mergeMertens.push(frames.last())
        if (!removedFrame.empty()) mergeMertens.pop()

        if (frames.size < size) return

        mergeMertens.process(hdrFrame)

        if (!hdrFrame.empty()) {
            hdrFrame.convertTo(outputFrame, frames[0].type(), 255.0)
            next(index, outputFrame)
        }
    }
}
