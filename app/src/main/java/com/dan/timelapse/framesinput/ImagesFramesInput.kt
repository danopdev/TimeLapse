package com.dan.timelapse.framesinput

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.dan.timelapse.utils.UriFile
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc.COLOR_RGBA2RGB
import org.opencv.imgproc.Imgproc.cvtColor
import java.io.FileNotFoundException

class ImagesFramesInput(private val _uriFiles: List<UriFile>) : FramesInput() {

    companion object {
        private fun fromUriFiles(uriFiles: List<UriFile>): ImagesFramesInput {
            val files = uriFiles.filter { it.isPhoto && !it.name.startsWith('.') }
                .sortedBy { it.name }

            if (files.isEmpty()) throw FileNotFoundException()

            return ImagesFramesInput(files)
        }

        fun fromFolder(folderUri: UriFile): ImagesFramesInput {
            return fromUriFiles(folderUri.listFiles())
        }

        fun fromFiles(context: Context, uris: List<Uri>): ImagesFramesInput {
            val files = uris.mapNotNull { UriFile.fromSingleUri(context, it) }
            return fromUriFiles(files)
        }

        private fun loadImage(uriFile: UriFile, frame: Mat) {
            val inputStream = uriFile.context.contentResolver.openInputStream(uriFile.uri) ?: throw FileNotFoundException()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (null == bitmap) throw FileNotFoundException()
            val image = Mat()
            Utils.bitmapToMat(bitmap, image)
            cvtColor(image, frame, COLOR_RGBA2RGB)
            image.release()
        }
    }

    private val _width: Int
    private val _height: Int
    private val _name: String

    override val fps: Int
        get() = 30

    override val name: String
        get() = _name

    override val width: Int
        get() = _width

    override val height: Int
        get() = _height

    override val size: Int
        get() = _uriFiles.size

    override val videoUri: Uri?
        get() = null

    init {
        _name = fixName(_uriFiles.first().name)
        val firstImage = Mat()
        loadImage(_uriFiles.first(), firstImage)
        _width = firstImage.width()
        _height = firstImage.height()
        firstImage.release()
    }

    override fun forEachFrame(callback: (Int, Int, Mat)->Boolean) {
        var counter = 0
        val frame = Mat()
        for(uriFile in _uriFiles) {
            loadImage(uriFile, frame)
            if (!callback( counter, _uriFiles.size, frame )) break
            counter++
        }
        frame.release()
    }
}