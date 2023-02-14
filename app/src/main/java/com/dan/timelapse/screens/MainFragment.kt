package com.dan.timelapse.screens

import android.content.Intent
import android.media.AudioManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.dan.timelapse.*
import com.dan.timelapse.databinding.MainFragmentBinding
import com.dan.timelapse.filters.*
import com.dan.timelapse.framesinput.FramesInput
import com.dan.timelapse.framesinput.ImagesFramesInput
import com.dan.timelapse.framesinput.VideoFramesInput
import com.dan.timelapse.utils.OutputParams
import com.dan.timelapse.utils.Settings
import com.dan.timelapse.video.VideoTools
import com.dan.timelapse.video.VideoWriter
import kotlinx.coroutines.*
import org.opencv.core.Mat
import java.io.File
import java.io.FileNotFoundException
import java.lang.Integer.min


class MainFragment(activity: MainActivity) : AppFragment(activity) {
    companion object {
        private const val INTENT_OPEN_VIDEO = 2
        private const val INTENT_OPEN_IMAGES = 3
        private const val INTENT_OPEN_FOLDER = 4

        private const val TITLE_GENERATE = "Generate"
        private const val TITLE_SAVE = "Save"

        private const val ORIENTATION_LANDSCAPE = 1
        private const val ORIENTATION_PORTRAIT = 2

        enum class Effect {
            NONE,
            AVERAGE,
            ENDLESS_AVERAGE,
            AVERAGE_WEIGHTED_FOR_LAST,
            ENDLESS_AVERAGE_WEIGHTED_FOR_LAST,
            AVERAGE_WEIGHTED_FOR_LIGHT,
            ENDLESS_AVERAGE_WEIGHTED_FOR_LIGHT,
            LIGHTEST_PIXELS,
            ENDLESS_LIGHTEST_PIXELS,
            HDR,
            TRANSITION
        }

        private val effectsWithoutSize = setOf(
            Effect.NONE,
            Effect.ENDLESS_AVERAGE,
            Effect.ENDLESS_AVERAGE_WEIGHTED_FOR_LAST,
            Effect.ENDLESS_AVERAGE_WEIGHTED_FOR_LIGHT,
            Effect.ENDLESS_LIGHTEST_PIXELS
        )

        fun show(activity: MainActivity) {
            activity.pushView("TimeLapse", MainFragment(activity))
        }
    }

    private lateinit var binding: MainFragmentBinding
    private var menuSave: MenuItem? = null
    private var framesInput: FramesInput? = null
    private var firstFrame = Mat()
    private val firstFrameMask = Mat()
    private val alignCache = AlignCache()
    private var outputParams: OutputParams? = null
    private var alignMaskId = 0

    private val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            updateView()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }

    }

    private val spinnerOnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            updateView()
        }

        override fun onNothingSelected(parent: AdapterView<*>) {
        }
    }

    private val tmpFolder: File
        get() = requireContext().cacheDir
    private val tmpOutputVideo: File
        get() = File(tmpFolder, "tmp_video.mp4")

    private val currentEffect: Effect
        get() = Effect.values().first { it.ordinal == binding.spinnerEffect.selectedItemPosition }

    private fun videoPlayOriginal() {
        videoStop()
        val videoUri = framesInput?.videoUri ?: return
        binding.video.setVideoURI(videoUri)
        binding.video.start()
    }

    private fun getCurrentOutputParams(framesInput: FramesInput): OutputParams {
        val outputParams = OutputParams()

        outputParams.set(OutputParams.KEY_H265, if(settings.h265) 1 else 0)
        outputParams.set(OutputParams.KEY_CROP, if(settings.crop) 1 else 0)
        outputParams.set(OutputParams.KEY_4K, if(settings.encode4K) 1 else 0)

        outputParams.set(OutputParams.KEY_SPEED, binding.seekBarSpeed.progress + 1)
        outputParams.set(OutputParams.KEY_ALIGN, if (binding.switchAlign.isChecked) alignMaskId else -1)

        val effect = currentEffect
        outputParams.set(OutputParams.KEY_EFFECT, effect.ordinal)
        outputParams.set(OutputParams.KEY_EFFECT_SIZE, if (effectsWithoutSize.contains(effect)) 0 else binding.seekBarEffect.progress + 2)

        outputParams.set(OutputParams.KEY_DURATION, binding.seekBarDuration.progress)

        val fps = Settings.FPS_VALUES[binding.seekBarFPS.progress]
        outputParams.set(OutputParams.KEY_FPS, fps)

        val orientation = when(binding.spinnerOrientation.selectedItemPosition) {
            ORIENTATION_LANDSCAPE -> ORIENTATION_LANDSCAPE
            ORIENTATION_PORTRAIT -> ORIENTATION_PORTRAIT
            else -> {
                if (framesInput.width >= framesInput.height)
                    ORIENTATION_LANDSCAPE
                else
                    ORIENTATION_PORTRAIT
            }
        }

        outputParams.set(OutputParams.KEY_ORIENTATION, orientation)

        return outputParams
    }

    private fun stabChangeFpsAsync(outputParams: OutputParams) {
        this.outputParams = null
        VideoTools.changeFps(tmpOutputVideo.absolutePath, outputParams.get(OutputParams.KEY_FPS))
        if (tmpOutputVideo.exists()) this.outputParams = outputParams
    }

    private fun changeFps(outputParams: OutputParams) {
        runAsync(TITLE_GENERATE, -1, true) {
            stabChangeFpsAsync(outputParams)
        }
    }

    private fun videoPlayGenerated(generateIfNeeded: Boolean) {
        val framesInput = this.framesInput ?: return

        videoStop()

        val outputParams = getCurrentOutputParams(framesInput)
        val changes = outputParams.compareWith(this.outputParams)

        if (!tmpOutputVideo.exists() || OutputParams.COMPARE_NOT_CHANGED != changes) {
            if (generateIfNeeded) {
                if (OutputParams.COMPARE_CHANGED_ONLY_FPS == changes) {
                    changeFps(outputParams)
                } else {
                    handleGenerate(outputParams)
                }
            }

            return
        }

        binding.video.setVideoURI(Uri.fromFile(tmpOutputVideo))
        binding.video.start()
    }

    private fun videoStop() {
        binding.video.pause()
        binding.video.setVideoURI(null)
    }

    private fun cleanUp() {
        try {
            if (tmpOutputVideo.exists()) tmpOutputVideo.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        framesInput = null
        updateView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(inflater)

        binding.video.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE)
        binding.video.setOnPreparedListener { newMediaPlayer ->
            newMediaPlayer.setVolume(0.0f, 0.0f)
        }

        binding.seekBarFPS.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.seekBarSpeed.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.seekBarEffect.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.seekBarDuration.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.buttonPlayOriginal.setOnClickListener { videoPlayOriginal() }
        binding.buttonPlayGenerated.setOnClickListener { videoPlayGenerated(true) }
        binding.buttonStop.setOnClickListener { videoStop() }

        binding.switchAlign.setOnCheckedChangeListener { _, _ -> updateView()  }
        binding.buttonAlignMask.setOnClickListener {
            MaskEditFragment.show(activity, firstFrame, firstFrameMask) {
                alignMaskId++
                alignCache.reset()
            }
        }

        binding.spinnerEffect.onItemSelectedListener = spinnerOnItemSelectedListener

        cleanUp()

        setHasOptionsMenu(true)

        openIntent(activity.intent)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.app_menu, menu)

        menuSave = menu.findItem(R.id.menuSave)

        updateView()
    }

    override fun onDestroyOptionsMenu() {
        menuSave = null
        super.onDestroyOptionsMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menuOpenVideo -> {
                handleOpenVideo()
                return true
            }

            R.id.menuOpenPhotos -> {
                handleOpenFiles()
                return true
            }

            R.id.menuOpenFolder -> {
                handleOpenFolder()
                return true
            }

            R.id.menuSave -> {
                handleSave()
                return true
            }

            R.id.menuSettings -> {
                SettingsFragment.show(activity)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun openIntent(intent: Intent?) {
        if (null == intent) return

        val context = requireContext()
        val uri = intent.data

        if (null != uri) {
            val mimeType = DocumentFile.fromSingleUri(context, uri)?.type
            if (null != mimeType && mimeType.startsWith("video/")) {
                openVideoFile(uri)
                return
            }

            val document = DocumentFile.fromTreeUri(context, uri) ?: return
            if (document.isDirectory) {
                openImageFolder(uri)
            }

            return
        }

        intent.clipData?.let { clipData ->
            if (clipData.itemCount > 0) {
                val uriList = mutableListOf<Uri>()
                for (i in 0 until clipData.itemCount) {
                    uriList.add(clipData.getItemAt(i).uri)
                }

                if (clipData.description.hasMimeType("video/*")) {
                    openVideoFile(uriList[0])
                } else if (clipData.description.hasMimeType("image/*")) {
                    openImagesFiles(uriList)
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (AppCompatActivity.RESULT_OK == resultCode) {
            openIntent(intent)
            return
        }

        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, intent)
    }

    private fun handleOpenVideo() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                .putExtra("android.content.extra.SHOW_ADVANCED", true)
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                .putExtra(Intent.EXTRA_TITLE, "Select video")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("video/*")
        @Suppress("DEPRECATION")
        startActivityForResult(intent, INTENT_OPEN_VIDEO)
    }

    private fun handleOpenFiles() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .putExtra("android.content.extra.SHOW_ADVANCED", true)
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            .putExtra(Intent.EXTRA_TITLE, "Select images")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("image/*")
        @Suppress("DEPRECATION")
        startActivityForResult(intent, INTENT_OPEN_IMAGES)
    }

    private fun handleOpenFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            .putExtra("android.content.extra.SHOW_ADVANCED", true)
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
            .putExtra(Intent.EXTRA_TITLE, "Select folder")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        @Suppress("DEPRECATION")
        startActivityForResult(intent, INTENT_OPEN_FOLDER)
    }

    private fun openVideoFile(uri: Uri) {
        cleanUp()
        try {
            setFramesInput( VideoFramesInput(requireContext(), uri) )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openImagesFiles(uris: List<Uri>) {
        cleanUp()
        try {
            setFramesInput( ImagesFramesInput(requireContext(), uris) )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openImageFolder(folderUri: Uri) {
        cleanUp()
        try {
            setFramesInput( ImagesFramesInput.fromFolder(requireContext(), folderUri) )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setFramesInput(framesInput: FramesInput) {
        outputParams = null
        alignCache.resetAll()
        this.framesInput = framesInput
        //binding.seekBarSpeed.progress = 0
        //binding.seekBarEffect.progress = 0
        binding.seekBarFPS.progress = Settings.getClosestFpsIndex(framesInput.fps)
        //binding.switchAlign.isEnabled = false
        firstFrame = framesInput.firstFrame()
        firstFrameMask.release()
        updateView()
    }

    private fun handleGenerate(outputParams: OutputParams) {
        runAsync(TITLE_GENERATE, 0, true) {
            generateAsync(outputParams)
        }
    }

    private fun handleSave() {
        videoStop()
        runAsync(TITLE_SAVE) {
            saveAsync()
        }
    }

    private fun saveAsync() {
        var success = false
        val outputFile = getOutputFile()

        try {
            val inputStream = tmpOutputVideo.inputStream()
            val outputStream = outputFile.outputStream()
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()

            //Add it to gallery
            MediaScannerConnection.scanFile(context, arrayOf(outputFile.absolutePath), null, null)

            success = true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        showToast(if (success) "Saved: ${outputFile.absolutePath}" else "Failed !")
    }

    private fun createOutputFolder() {
        if (!Settings.SAVE_FOLDER.exists()) Settings.SAVE_FOLDER.mkdirs()
    }

    private fun buildFile(suffix: String, counter: Int): File {
        return File(Settings.SAVE_FOLDER, suffix + (if (0 == counter) "" else "_${String.format("%03d", counter)}") + ".mp4")
    }

    private fun getOutputFile(): File {
        val framesInput = this.framesInput ?: throw FileNotFoundException()
        createOutputFolder()

        var outputFile = buildFile(framesInput.name, 0)
        var counter = 0

        while(outputFile.exists() && counter < 998) {
            counter++
            outputFile = buildFile(framesInput.name, counter)
        }

        return outputFile
    }

    private fun generateAsync(outputParams: OutputParams) {
        this.outputParams = null
        val framesInput = this.framesInput ?: return
        var success = false

        try {
            if (tmpOutputVideo.exists()) tmpOutputVideo.delete()

            val fps = Settings.FPS_VALUES[binding.seekBarFPS.progress]

            var videoWidth = 1920
            var videoHeight = 1080

            if (outputParams.get(OutputParams.KEY_4K) != 0) {
                videoWidth *= 2
                videoHeight *= 2
            }

            if (ORIENTATION_PORTRAIT == outputParams.get(OutputParams.KEY_ORIENTATION)) {
                val tmp = videoWidth
                videoWidth = videoHeight
                videoHeight = tmp
            }

            var frameConsumer: FramesConsumer = VideoWriter(
                tmpOutputVideo.absolutePath,
                fps,
                settings.h265 )

            if (binding.spinnerEffect.selectedItemPosition > 0) {
                val effectSize = binding.seekBarEffect.progress + 2
                when (currentEffect) {
                    Effect.NONE -> {} //avoid warning
                    Effect.AVERAGE -> frameConsumer = AverageFramesFilter(effectSize, frameConsumer)
                    Effect.ENDLESS_AVERAGE -> frameConsumer = EndlessAverageFramesFilter(frameConsumer)
                    Effect.AVERAGE_WEIGHTED_FOR_LAST -> frameConsumer = AverageWeightedForLastFramesFilter(effectSize, frameConsumer)
                    Effect.ENDLESS_AVERAGE_WEIGHTED_FOR_LAST -> frameConsumer = EndlessAverageWeightedForLastFramesFilter(frameConsumer)
                    Effect.AVERAGE_WEIGHTED_FOR_LIGHT -> frameConsumer = AverageWeightedForLightFramesFilter(effectSize, frameConsumer)
                    Effect.ENDLESS_AVERAGE_WEIGHTED_FOR_LIGHT -> frameConsumer = EndlessAverageWeightedForLightFramesFilter(frameConsumer)
                    Effect.LIGHTEST_PIXELS -> frameConsumer = LightestPixelsFramesFilter(effectSize, frameConsumer)
                    Effect.ENDLESS_LIGHTEST_PIXELS -> frameConsumer = EndlessLightestPixelsFramesFilter(frameConsumer)
                    Effect.HDR -> frameConsumer = HDRFramesFilter(effectSize, frameConsumer)
                    Effect.TRANSITION -> frameConsumer = TransitionFramesFilter(effectSize, frameConsumer)
                }
            }

            if (binding.switchAlign.isChecked) {
                frameConsumer = AlignFramesFilter(firstFrameMask, alignCache, frameConsumer)
            }

            frameConsumer = ScaleFramesFilter( settings.crop, videoWidth, videoHeight, frameConsumer )

            if (binding.seekBarSpeed.progress > 0) {
                frameConsumer = SampleFramesFilter( binding.seekBarSpeed.progress + 1, frameConsumer )
            }

            val maxFrames = binding.seekBarDuration.progress * framesInput.fps

            BusyDialog.showCancel()
            frameConsumer.start()
            framesInput.forEachFrame { index, size_, frame ->
                val size = if (0 == maxFrames) size_ else min(size_, maxFrames)
                BusyDialog.show(TITLE_GENERATE, index, size)

                if (BusyDialog.isCanceled()) {
                    false
                } else if (maxFrames > 0 && index >= maxFrames) {
                    false
                }
                else {
                    frameConsumer.consume(index, frame)
                    true
                }
        }
            frameConsumer.stop()

            if (!BusyDialog.isCanceled()) {
                this.outputParams = outputParams
                success = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (!success && tmpOutputVideo.exists()) {
            tmpOutputVideo.delete()
        }

        runOnUiThread {
            updateView()
        }
    }

    private fun runAsync(initialMessage: String, progress: Int, playOnFinish: Boolean, asyncTask: () -> Unit) {
        videoStop()

        GlobalScope.launch(Dispatchers.Default) {
            try {
                BusyDialog.show(initialMessage, progress, framesInput?.size ?: -1)
                asyncTask()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            runOnUiThread {
                updateView()
                BusyDialog.dismiss()
                if (playOnFinish) videoPlayGenerated(false)
            }
        }
    }

    private fun runAsync(initialMessage: String, asyncTask: () -> Unit) {
        runAsync(initialMessage, -1, false, asyncTask)
    }

    private fun updateView() {
        val framesInput = this.framesInput
        val enabled = null != framesInput
        binding.seekBarSpeed.isEnabled = enabled
        binding.spinnerEffect.isEnabled = enabled
        binding.seekBarEffect.isEnabled = enabled && !effectsWithoutSize.contains(currentEffect)
        binding.seekBarFPS.isEnabled = enabled
        binding.seekBarDuration.isEnabled = enabled
        binding.switchAlign.isEnabled = enabled
        binding.buttonAlignMask.isEnabled = enabled && binding.switchAlign.isChecked && !firstFrame.empty()

        if (null == framesInput) {
            binding.textInfo.text = ""
        } else if (binding.textInfo.text.isEmpty()) {
            binding.textInfo.text = "${framesInput.width} x ${framesInput.height}, ${framesInput.fps} FPS, ${framesInput.name}"
        }

        val hasInputVideo = enabled && null != framesInput?.videoUri
        val hasGeneratedVideo = enabled && tmpOutputVideo.exists()
        menuSave?.isEnabled = hasGeneratedVideo
        binding.buttonPlayOriginal.isEnabled = hasInputVideo
        binding.buttonPlayGenerated.isEnabled = enabled
        binding.buttonStop.isEnabled = enabled
        binding.video.setVideoURI(null)

        binding.textSpeed.text = "${binding.seekBarSpeed.progress + 1}x"
        binding.textEffect.text = "${binding.seekBarEffect.progress + 2}x"
        binding.textFPS.text = Settings.FPS_VALUES[binding.seekBarFPS.progress].toString()
        binding.textDuration.text = if (0 == binding.seekBarDuration.progress) "all" else "${binding.seekBarDuration.progress} s"
    }
}