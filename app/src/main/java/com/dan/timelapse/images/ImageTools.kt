package com.dan.timelapse.images

import org.opencv.core.Mat
import org.opencv.utils.Converters

class ImageTools {
    companion object {
        fun mergeLightestPixels(images: List<Mat>, output: Mat) {
            val imagesMat = Converters.vector_Mat_to_Mat(images)
            mergeLightestPixelsNative(imagesMat.nativeObj, output.nativeObj)
        }

        private external fun mergeLightestPixelsNative(images: Long, output: Long)
    }
}