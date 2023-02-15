package com.dan.timelapse.images

import android.graphics.Bitmap
import com.dan.timelapse.filters.FramesConsumer
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File

class ImageWriter(private val file: File, private val jpegQuality: Int)
    : FramesConsumer {

    private var lastFrame = Mat()
    private var _success = false

    val success: Boolean
        get() = _success

    override fun start() {
    }

    override fun stop(canceled: Boolean) {
        if (!canceled && !lastFrame.empty()) {
            val bitmap = Bitmap.createBitmap(
                lastFrame.width(),
                lastFrame.height(),
                Bitmap.Config.ARGB_8888
            )

            Utils.matToBitmap( lastFrame, bitmap)

            try {
                val outputStream = file.outputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality, outputStream)
                outputStream.close()
                _success = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        lastFrame.release()
    }

    override fun consume(index: Int, frame: Mat) {
        lastFrame.create(frame.size(), frame.type())
        frame.copyTo(lastFrame)
    }
}