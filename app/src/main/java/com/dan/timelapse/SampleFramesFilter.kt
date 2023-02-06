package com.dan.timelapse

import org.opencv.core.Mat

class SampleFramesFilter(private val sample: Int, nextConsumer: FramesConsumer): FramesFilter(nextConsumer) {
    private var counter = 0

    override fun startFilter() {
        counter = 0
        super.startFilter()
    }

    override fun consume(index: Int, frame: Mat) {
        if (0 == counter) next(index, frame)
        counter++
        if (counter >= sample) counter = 0
    }
}