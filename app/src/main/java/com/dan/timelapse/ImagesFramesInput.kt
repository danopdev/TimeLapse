package com.dan.timelapse

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.FileNotFoundException

class ImagesFramesInput(private val context: Context, inputUris: List<Uri>) : FramesInput {

    companion object {
        fun fromFolder(context: Context, folderUri: Uri): ImagesFramesInput {
            val document = DocumentFile.fromTreeUri(context, folderUri) ?: throw FileNotFoundException()
            val files = document.listFiles()
                .filter { file -> file.type?.startsWith("image/") ?: false }
                .map { file -> file.uri }
            return ImagesFramesInput(context, files)
        }

        private fun loadImage(context: Context, uri: Uri): Mat {
            val inputStream = context.contentResolver.openInputStream(uri) ?: throw FileNotFoundException()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (null == bitmap) throw FileNotFoundException()
            val image = Mat()
            Utils.bitmapToMat(bitmap, image)
            return image
        }
    }

    private val _name: String
    private val _uris: List<Uri>
    private val _width: Int
    private val _height: Int

    override val fps: Int
        get() = 5

    override val name: String
        get() = _name

    override val width: Int
        get() = _width

    override val height: Int
        get() = _height

    private fun sortUris(inputUris: List<Uri>): Pair<String, List<Uri>> {
        val urisWithNames = inputUris.map { uri ->
            val document = DocumentFile.fromSingleUri(context, uri) ?: throw FileNotFoundException()
            val name = document.name ?: throw FileNotFoundException()
            Pair(name, uri)
        }

        val sorted = urisWithNames.sortedBy { it.first }
        val sortedList = sorted.map{ pair -> pair.second }
        return Pair(urisWithNames[0].first, sortedList)
    }

    init {
        if (inputUris.isEmpty()) throw FileNotFoundException()
        val sortedResult = sortUris(inputUris)
        _name = FramesInput.fixName(sortedResult.first)
        _uris = sortedResult.second

        val firstImage = loadImage(context, _uris[0])
        _width = firstImage.width()
        _height = firstImage.height()
    }

    override fun forEachFrame(callback: (Mat) -> Unit) {
        for(uri in _uris) {
            callback( loadImage(context, uri) )
        }
    }
}