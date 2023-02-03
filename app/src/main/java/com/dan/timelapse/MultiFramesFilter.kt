package com.dan.timelapse

import org.opencv.core.Mat

abstract class MultiFramesFilter(private val size: Int, private val notifyOnPartial: Boolean, nextConsumer: FramesConsumer)
    : FramesFilter(nextConsumer) {

    private val frames = mutableListOf<Mat>()

    abstract fun consume(removedFrame: Mat?, frames: List<Mat>)

    override fun consume(frame: Mat) {
        frames.add(frame.clone())

        var removedFrame: Mat? = null

        if (frames.size > size) {
            removedFrame = frames.removeFirst()
        }

        if (frames.size >= size || notifyOnPartial) {
            consume(removedFrame, frames)
        }

        removedFrame?.release() //force to free memory immediately
    }

    override fun stopFilter() {
        frames.forEach{ frame -> frame.release() } //force to free memory immediately
        frames.clear()

        super.stopFilter()
    }
}