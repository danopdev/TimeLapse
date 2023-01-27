package com.dan.timelapse

import android.content.Intent
import android.media.AudioManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.dan.timelapse.databinding.MainFragmentBinding
import kotlinx.coroutines.*
import java.io.File
import java.io.FileNotFoundException


class MainFragment(activity: MainActivity) : AppFragment(activity) {
    companion object {
        const val INTENT_OPEN_VIDEO = 2
        const val INTENT_OPEN_IMAGES = 3
        const val INTENT_OPEN_FOLDER = 4

        const val TITLE_GENERATE = "Generate"
        const val TITLE_SAVE = "Save"

        fun show(activity: MainActivity) {
            activity.pushView("TimeLapse", MainFragment(activity))
        }
    }

    private lateinit var binding: MainFragmentBinding
    private var menuSave: MenuItem? = null
    private var framesInput: FramesInput? = null
    private var videoUri: Uri? = null

    private val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            updateView()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }

    }

    private val tmpFolder: File
        get() = requireContext().cacheDir
    private val tmpOutputVideo: File
        get() = File(tmpFolder, "tmp_video.mp4")

    private fun videoPlay() {
        binding.video.start()
    }

    private fun videoStop() {
        binding.video.pause()
        binding.video.seekTo(0)
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

        binding.buttonOpenVideo.setOnClickListener { handleOpenVideo() }
        binding.buttonOpenImageFiles.setOnClickListener { handleOpenFiles() }
        binding.buttonOpenImageFolder.setOnClickListener { handleOpenFolder() }

        binding.video.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE)
        binding.video.setOnPreparedListener { newMediaPlayer ->
            newMediaPlayer.setVolume(0.0f, 0.0f)
        }

        binding.seekBarFPS.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.seekBarSpeed.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.seekBarSmooth.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.seekBarTransition.setOnSeekBarChangeListener(seekBarChangeListener)

        binding.buttonGenerate.setOnClickListener { handleGenerate() }

        binding.buttonPlay.setOnClickListener { videoPlay() }
        binding.buttonStop.setOnClickListener { videoStop() }

        cleanUp()

        setHasOptionsMenu(true)

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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (AppCompatActivity.RESULT_OK == resultCode) {
            when (requestCode) {
                INTENT_OPEN_VIDEO -> {
                    intent?.data?.let { uri -> openVideoFile(uri) }
                    return
                }

                INTENT_OPEN_IMAGES -> {
                    intent?.clipData?.let { clipData ->
                        val uriList = mutableListOf<Uri>()
                        val count = clipData.itemCount
                        for (i in 0 until count) {
                            uriList.add(clipData.getItemAt(i).uri)
                        }
                        openImagesFiles(uriList.toList())
                    }
                    return
                }

                INTENT_OPEN_FOLDER -> {
                    intent?.data?.let { uri -> openImageFolder(uri) }
                    return
                }
            }
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
        this.framesInput = framesInput
        binding.seekBarSpeed.progress = 0
        binding.seekBarSmooth.progress = 0
        binding.seekBarTransition.progress = 0
        binding.seekBarFPS.progress = Settings.getClosestFpsIndex(framesInput.fps)
        updateView()
    }

    private fun handleGenerate() {
        videoStop()
        videoUri = null
        binding.video.setVideoURI(null)
        runAsync(TITLE_GENERATE) {
            generateAsync()
        }
    }

    private fun handleSave() {
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

    private fun generateAsync() {
        val framesInput = this.framesInput ?: return
        var success = false

        try {
            if (tmpOutputVideo.exists()) tmpOutputVideo.delete()

            val fps = Settings.FPS_VALUES[binding.seekBarFPS.progress]

            var videoWidth = 1920
            var videoHeight = 1080

            if (settings.encode4K) {
                videoWidth *= 2
                videoHeight *= 2
            }

            if (framesInput.width < framesInput.height) {
                val tmp = videoWidth
                videoWidth = videoHeight
                videoHeight = tmp
            }

            var frameConsumer: FramesConsumer = VideoWriter(
                tmpOutputVideo.absolutePath,
                fps,
                videoWidth,
                videoHeight,
                settings.h265 )

            if (binding.seekBarTransition.progress > 0) {
                frameConsumer = TransitionFramesFilter( binding.seekBarTransition.progress + 1, frameConsumer )
            }

            if (binding.seekBarSmooth.progress > 0) {
                frameConsumer = SmoothFramesFilter( binding.seekBarSmooth.progress + 1, frameConsumer )
            }

            frameConsumer = ScaleFramesFilter( settings.crop, videoWidth, videoHeight, frameConsumer )

            if (binding.seekBarSpeed.progress > 0) {
                frameConsumer = SampleFramesFilter( binding.seekBarSpeed.progress + 1, frameConsumer )
            }

            frameConsumer.start()
            framesInput.forEachFrame { index, size, frame ->
                BusyDialog.show(TITLE_GENERATE, index, size)
                frameConsumer.consume(frame)
            }
            frameConsumer.stop()

            success = true
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

    private fun runAsync(initialMessage: String, asyncTask: () -> Unit) {
        videoStop()

        GlobalScope.launch(Dispatchers.Default) {
            try {
                BusyDialog.show(initialMessage)
                asyncTask()
            } catch (e: Exception) {
                //TODO
            }

            runOnUiThread {
                updateView()
                BusyDialog.dismiss()
            }
        }
    }

    private fun updateView() {
        val framesInput = this.framesInput
        val enabled = null != framesInput
        menuSave?.isEnabled = enabled
        binding.seekBarSpeed.isEnabled = enabled
        binding.seekBarSmooth.isEnabled = enabled
        binding.seekBarTransition.isEnabled = enabled
        binding.seekBarFPS.isEnabled = enabled
        binding.buttonGenerate.isEnabled = enabled

        if (null == framesInput) {
            binding.textInfo.text = ""
        } else if (binding.textInfo.text.isEmpty()) {
            binding.textInfo.text = "${framesInput.width} x ${framesInput.height}, ${framesInput.fps} FPS, ${framesInput.name}"
        }

        val hasVideo = enabled && tmpOutputVideo.exists()
        binding.buttonPlay.isEnabled = hasVideo
        binding.buttonStop.isEnabled = hasVideo

        binding.textSpeed.text = "${binding.seekBarSpeed.progress + 1}x"
        binding.textSmooth.text = "${binding.seekBarSmooth.progress + 1}x"
        binding.textTransition.text = "${binding.seekBarTransition.progress + 1}"
        binding.textFPS.text = Settings.FPS_VALUES[binding.seekBarFPS.progress].toString()

        if (hasVideo) {
            if (null == videoUri) {
                videoUri = Uri.fromFile(tmpOutputVideo)
                binding.video.setVideoURI(videoUri)
            }
        } else {
            videoUri = null
            binding.video.setVideoURI(null)
        }
    }
}
