package com.dan.timelapse

import org.opencv.core.Mat

abstract class MultiFramesFilter(private val size: Int, private val notifyOnPartial: Boolean, nextConsumer: FramesConsumer)
    : FramesFilter(nextConsumer) {

    private val frames = mutableListOf<Mat>()

    abstract fun consume(index: Int, removedFrame: Mat, frames: List<Mat>)

    override fun consume(index: Int, frame: Mat) {
        frames.add(frame.clone())

        val removedFrame = if (frames.size > size) frames.removeFirst() else Mat()

        if (frames.size >= size || notifyOnPartial) {
            consume(index, removedFrame, frames)
        }

        removedFrame.release() //force to free memory immediately
    }

    override fun stopFilter() {
        frames.forEach{ frame -> frame.release() } //force to free memory immediately
        frames.clear()

        super.stopFilter()
    }
}