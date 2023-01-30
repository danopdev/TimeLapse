package com.dan.timelapse

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri

class VideoTools {
    companion object {
        fun countFrames(context: Context, uri: Uri): Int {
            var counter = 0

            try {
                val extractor = MediaExtractor()
                extractor.setDataSource(context, uri, null)

                for (trackIndex in 0 until extractor.trackCount) {
                    val trackFormat = extractor.getTrackFormat(trackIndex)
                    val trackMime = trackFormat.getString(MediaFormat.KEY_MIME) ?: continue
                    if (!trackMime.startsWith("video/")) continue
                    extractor.selectTrack(trackIndex)
                    while (extractor.advance()) {
                        counter++
                    }
                    break
                }

                extractor.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return counter
        }
    }
}