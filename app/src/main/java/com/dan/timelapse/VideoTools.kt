package com.dan.timelapse

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import java.io.File
import java.nio.ByteBuffer

class VideoTools {
    companion object {
        private const val BUFFER_SIZE = 5 * 1024 * 1024 //1 MB

        fun countFrames(context: Context, uri: Uri): Int {
            var counter = 1

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

        fun changeFps(tmpFile: String, fps: Int) {
            val copyTmpFile = "$tmpFile.tmp"
            File(tmpFile).renameTo(File(copyTmpFile))
            var success = false
            val buffer = ByteBuffer.allocate(BUFFER_SIZE)
            val bufferInfo = MediaCodec.BufferInfo()

            try {
                val extractor = MediaExtractor()
                extractor.setDataSource(copyTmpFile)
                val trackFormat = extractor.getTrackFormat(0)
                extractor.selectTrack(0)

                val muxer = MediaMuxer(tmpFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                val destTrackIndex = muxer.addTrack(trackFormat)
                muxer.start()

                var outputIndex = 0L

                while(true) {
                    val readSize = extractor.readSampleData(buffer, 0)
                    if (readSize <= 0) break

                    bufferInfo.set(
                        0,
                        readSize,
                        outputIndex * 1000000L / fps.toLong(),
                        if (0 != (extractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC)) MediaCodec.BUFFER_FLAG_KEY_FRAME else 0)
                    muxer.writeSampleData(destTrackIndex, buffer, bufferInfo)

                    if (!extractor.advance()) break
                    outputIndex++
                }

                muxer.stop()

                extractor.release()
                success = true
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (!success) {
                File(tmpFile).delete()
            }

            File(copyTmpFile).delete()
        }
    }
}