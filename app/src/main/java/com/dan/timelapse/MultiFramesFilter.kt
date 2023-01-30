package com.dan.timelapse

import org.opencv.core.Mat

abstract class MultiFramesFilter(private val size: Int, private val notifyOnPartial: Boolean, nextConsumer: FramesConsumer)
    : FramesFilter(nextConsumer) {

    private val frames = arrayOfNulls<Mat?>(size)
    private var index = 0
    private var nbOfValidFrames = 0

    abstract fun consume(removedFrame: Mat?, lastFrame: Mat?, newFrame: Mat, allFrames: Array<Mat?>, nbOfValidFrames: Int)

    override fun consume(frame: Mat) {
        val removedFrame = frames[index]
        val newFrame = frame.clone()
        frames[index] = newFrame
        index = (index + 1) % size
        if (nbOfValidFrames < size) nbOfValidFrames++

        if (null == frames[index] && !notifyOnPartial) return

        consume(removedFrame, frames[index], newFrame, frames, nbOfValidFrames)
    }
}