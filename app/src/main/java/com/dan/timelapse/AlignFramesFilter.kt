package com.dan.timelapse

import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.video.Video

class AlignFramesFilter(private val fullMask: Mat, nextConsumer: FramesConsumer)
    : FramesFilter(nextConsumer) {

    private val firstGrayFrame = Mat()
    private val firstFramePts = MatOfPoint2f()
    private val grayFrame = Mat()
    private val alignedFrame = Mat()
    private val mask = Mat()

    override fun consume(frame: Mat) {
        if (firstGrayFrame.empty()) {
            if (!fullMask.empty()) {
                Imgproc.resize(fullMask, mask, Size(frame.width().toDouble(), frame.height().toDouble()), 0.0, 0.0, Imgproc.INTER_NEAREST )
            }

            Imgproc.cvtColor(frame, firstGrayFrame, Imgproc.COLOR_BGR2GRAY)

            val pts = MatOfPoint()
            Imgproc.goodFeaturesToTrack(firstGrayFrame, pts, 200, 0.01, 30.0, mask)

            firstFramePts.fromList(pts.toList())
            next(frame)
            return
        }

        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY)
        val framePts = MatOfPoint2f()
        val status = MatOfByte()
        val err = MatOfFloat()
        Video.calcOpticalFlowPyrLK(firstGrayFrame, grayFrame, firstFramePts, framePts, status, err)

        val byteZero = 0.toByte()
        val statusList = status.toList()
        val firstFramePtsValid = firstFramePts.toList().filterIndexed{ index, _ -> byteZero != statusList[index] }
        val framePtsValid = framePts.toList().filterIndexed{ index, _ -> byteZero != statusList[index] }

        // Find transformation matrix
        val firstFramePtsMat = MatOfPoint2f()
        val framePtsMat = MatOfPoint2f()

        firstFramePtsMat.fromList(firstFramePtsValid)
        framePtsMat.fromList(framePtsValid)

        val t = Calib3d.estimateAffinePartial2D(framePtsMat, firstFramePtsMat)
        if (t.empty()) {
            // failed to align
            next(frame)
            return
        }

        Imgproc.warpAffine(frame, alignedFrame, t, frame.size(), Imgproc.INTER_LANCZOS4)
        next(alignedFrame)
    }

    override fun startFilter() {
        firstGrayFrame.release()
        grayFrame.release()
        firstFramePts.release()
        alignedFrame.release()
        mask.release()
        super.startFilter()
    }
}