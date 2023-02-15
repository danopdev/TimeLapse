package com.dan.timelapse.filters

import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.video.Video


class AlignCache  {
    val firstGrayFrame = Mat()
    val firstFramePts = MatOfPoint2f()
    val alignmentMat = mutableListOf<Mat?>()

    fun resetIfSizeChanged(width: Int, height: Int) {
        if (width != firstGrayFrame.width() || height != firstGrayFrame.height()) {
            resetAll()
        }
    }

    fun resetAll() {
        firstGrayFrame.release()
        firstFramePts.release()
        reset()
    }

    fun reset() {
        for (mat in alignmentMat) mat?.release()
        alignmentMat.clear()
    }
}


class AlignFramesFilter(private val fullMask: Mat, private val cache: AlignCache, nextConsumer: FramesConsumer)
    : FramesFilter(nextConsumer) {

    companion object {
        private const val FIX_BORDER_SCALE_FACTOR = 1.05 //to remove black borders
        private const val byteZero = 0.toByte()
    }

    private val grayFrame = Mat()
    private val alignedFrame = Mat()
    private val alignedFrameFixed = Mat()
    private val mask = Mat()

    private fun fixAndNext(index: Int, frame: Mat) {
        val t = Imgproc.getRotationMatrix2D(Point(frame.width() / 2.0, frame.height() / 2.0), 0.0, FIX_BORDER_SCALE_FACTOR)
        Imgproc.warpAffine(frame, alignedFrameFixed, t, frame.size(), Imgproc.INTER_LANCZOS4)
        next(index, alignedFrameFixed)
    }

    override fun consume(index: Int, frame: Mat) {
        if (!fullMask.empty()) {
            Imgproc.resize(fullMask, mask, Size(frame.width().toDouble(), frame.height().toDouble()), 0.0, 0.0, Imgproc.INTER_NEAREST )
        }

        if (cache.firstGrayFrame.empty() || frame.width() != cache.firstGrayFrame.width() || frame.height() != cache.firstGrayFrame.height()) {
            cache.resetAll()

            Imgproc.cvtColor(frame, cache.firstGrayFrame, Imgproc.COLOR_BGR2GRAY)

            val pts = MatOfPoint()
            Imgproc.goodFeaturesToTrack(cache.firstGrayFrame, pts, 200, 0.01, 30.0, mask)
            cache.firstFramePts.fromList(pts.toList())
        }

        if (0 == index) {
            fixAndNext(index, frame)
            return
        }

        while(cache.alignmentMat.size <= index) {
            cache.alignmentMat.add(null)
        }

        var transformMat = cache.alignmentMat[index]
        if (null == transformMat) {
            Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY)
            val framePts = MatOfPoint2f()
            val status = MatOfByte()
            val err = MatOfFloat()
            Video.calcOpticalFlowPyrLK(cache.firstGrayFrame, grayFrame, cache.firstFramePts, framePts, status, err)

            val statusList = status.toList()
            val firstFramePtsValid = cache.firstFramePts.toList().filterIndexed { i, _ -> byteZero != statusList[i] }
            val framePtsValid = framePts.toList().filterIndexed { i, _ -> byteZero != statusList[i] }

            // Find transformation matrix
            val firstFramePtsMat = MatOfPoint2f()
            val framePtsMat = MatOfPoint2f()

            firstFramePtsMat.fromList(firstFramePtsValid)
            framePtsMat.fromList(framePtsValid)

            transformMat = Calib3d.estimateAffinePartial2D(framePtsMat, firstFramePtsMat)
            cache.alignmentMat[index] = transformMat
        }

        if (null == transformMat || transformMat.empty()) {
            // failed to align
            fixAndNext(index, frame)
            return
        }

        Imgproc.warpAffine(frame, alignedFrame, transformMat, frame.size(), Imgproc.INTER_LANCZOS4)
        fixAndNext(index, alignedFrame)
    }

    override fun startFilter() {
        grayFrame.release()
        alignedFrame.release()
        alignedFrameFixed.release()
        mask.release()
        super.startFilter()
    }
}