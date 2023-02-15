package com.dan.timelapse.framesinput

import android.net.Uri
import com.dan.timelapse.utils.UriFile
import com.dan.timelapse.video.VideoTools
import org.opencv.core.Mat
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.Videoio.*
import java.io.FileNotFoundException

class VideoFramesInput(private val _uriFile: UriFile) : FramesInput() {
    companion object {
        private fun open(uriFile: UriFile): VideoCapture {
            val pfd = uriFile.context.contentResolver.openFileDescriptor(uriFile.uri, "r") ?: throw FileNotFoundException()
            val fd = pfd.detachFd()
            val videoCapture = VideoCapture(":$fd")
            pfd.close()
            if (!videoCapture.isOpened) throw FileNotFoundException()
            videoCapture.set(CAP_PROP_ORIENTATION_AUTO, 1.0 )
            return videoCapture
        }

        private fun withVideoInput(uriFile: UriFile, callback: (VideoCapture)->Unit) {
            val videoInput = open(uriFile)
            callback(videoInput)
            videoInput.release()
        }
    }

    private var _fps: Int = 0
    private var _width: Int = 0
    private var _height: Int = 0
    private var _size: Int = 0
    private val _name: String

    override val fps: Int
        get() = _fps

    override val name: String
        get() = _name

    override val width: Int
        get() = _width

    override val height: Int
        get() = _height

    override val videoUri: Uri
        get() = _uriFile.uri

    override val size: Int
        get() = _size

    init {
        _name = fixName(_uriFile.name)

        withVideoInput(_uriFile) { videoInput ->
            _fps = videoInput.get(CAP_PROP_FPS).toInt()
            _width = videoInput.get(CAP_PROP_FRAME_WIDTH).toInt()
            _height = videoInput.get(CAP_PROP_FRAME_HEIGHT).toInt()
            _size = videoInput.get(CAP_PROP_FRAME_COUNT).toInt()
        }

        if (_size <= 0) _size = VideoTools.countFrames(_uriFile.context, _uriFile.uri)
    }

    override fun forEachFrame(callback: (Int, Int, Mat)->Boolean) {
        var counter = 0
        withVideoInput(_uriFile) { videoInput ->
            val frame = Mat()
            while(counter < _size && videoInput.read(frame) ) {
                if (!callback(counter, _size, frame)) break
                counter++
            }
            frame.release()
        }
    }
}