package com.dan.timelapse

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import org.opencv.core.Mat
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.Videoio.*
import java.io.FileNotFoundException

class VideoFramesInput( private val context: Context, private val uri: Uri) : FramesInput {
    companion object {
        private fun open(context: Context, uri: Uri): VideoCapture {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: throw FileNotFoundException()
            val fd = pfd.detachFd()
            val videoCapture = VideoCapture(":$fd")
            pfd.close()
            if (!videoCapture.isOpened) throw FileNotFoundException()
            videoCapture.set(CAP_PROP_ORIENTATION_AUTO, 1.0 )
            return videoCapture
        }

        private fun withVideoInput(context: Context, uri: Uri, callback: (VideoCapture)->Unit) {
            val videoInput = open(context, uri)
            callback(videoInput)
            videoInput.release()
        }
    }

    private var _fps: Int = 0
    private val _name: String
    private var _width: Int = 0
    private var _height: Int = 0
    private var _size: Int = 0

    override val fps: Int
        get() = _fps

    override val name: String
        get() = _name

    override val width: Int
        get() = _width

    override val height: Int
        get() = _height

    override val videoUri: Uri
        get() = uri

    init {
        val document = DocumentFile.fromSingleUri(context, uri) ?: throw FileNotFoundException()
        _name = FramesInput.fixName(document.name)
        withVideoInput(context, uri) { videoInput ->
            _fps = videoInput.get(CAP_PROP_FPS).toInt()
            _width = videoInput.get(CAP_PROP_FRAME_WIDTH).toInt()
            _height = videoInput.get(CAP_PROP_FRAME_HEIGHT).toInt()
            _size = videoInput.get(CAP_PROP_FRAME_COUNT).toInt()
        }
    }

    override fun forEachFrame(callback: (Int, Int, Mat) -> Unit) {
        var counter = 0
        withVideoInput(context, uri) { videoInput ->
            val frame = Mat()
            while(videoInput.read(frame)) {
                callback(counter, _size, frame)
                counter++
            }
        }
        if (counter > _size) _size = counter
    }
}