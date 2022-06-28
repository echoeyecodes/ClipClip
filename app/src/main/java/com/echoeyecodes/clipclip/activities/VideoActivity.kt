package com.echoeyecodes.clipclip.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.view.TextureView
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.echoeyecodes.clipclip.R
import com.echoeyecodes.clipclip.callbacks.*
import com.echoeyecodes.clipclip.customviews.videoselectionview.VideoSelectionCallback
import com.echoeyecodes.clipclip.customviews.videoselectionview.VideoSelectionView
import com.echoeyecodes.clipclip.databinding.ActivityVideoSelectionBinding
import com.echoeyecodes.clipclip.fragments.dialogfragments.ProgressDialogFragment
import com.echoeyecodes.clipclip.fragments.dialogfragments.VideoConfigurationDialogFragment
import com.echoeyecodes.clipclip.fragments.dialogfragments.VideoTimestampDialogFragment
import com.echoeyecodes.clipclip.fragments.videofragments.VideoCanvasFragment
import com.echoeyecodes.clipclip.models.VideoCanvasConfigModel
import com.echoeyecodes.clipclip.models.VideoCanvasModel
import com.echoeyecodes.clipclip.models.VideoEditConfigModel
import com.echoeyecodes.clipclip.models.VideoTrimConfigModel
import com.echoeyecodes.clipclip.trimmer.VideoTrimManager
import com.echoeyecodes.clipclip.utils.*
import com.echoeyecodes.clipclip.viewmodels.VideoActivityViewModel
import com.echoeyecodes.clipclip.viewmodels.VideoActivityViewModelFactory
import com.echoeyecodes.clipclip.workmanager.VideoTrimWorkManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Util
import com.google.android.material.button.MaterialButton
import kotlin.math.min

class VideoActivity : AppCompatActivity(), VideoSelectionCallback, Player.Listener,
    VideoConfigurationCallback, VideoTrimCallback, VideoTimestampCallback, VideoActivityCallback {
    private lateinit var videoTrimManager: VideoTrimManager
    private val binding by lazy { ActivityVideoSelectionBinding.inflate(layoutInflater) }
    private lateinit var textView: TextView
    private lateinit var timestamp: TextView
    private lateinit var bufferProgressContainer: View
    private lateinit var durationTextView: TextView
    private lateinit var doneBtn: MaterialButton
    private lateinit var timeBtn: View
    private lateinit var canvasBtn: View
    private lateinit var closeBtn: View
    private lateinit var videoSelectionView: VideoSelectionView
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var viewModel: VideoActivityViewModel
    private lateinit var progressDialogFragment: ProgressDialogFragment
    private lateinit var videoConfigurationDialogFragment: VideoConfigurationDialogFragment
    private lateinit var videoTimestampDialogFragment: VideoTimestampDialogFragment
    private val handler by lazy { Handler(mainLooper) }
    private val videoActivityCallbacks = ArrayList<VideoPlayerCallback>()

    private val playerRunnable = object : Runnable {
        override fun run() {
            checkPlayerProgress()
            handler.postDelayed(this, DELAY_MILLIS)
        }
    }

    companion object {
        const val DELAY_MILLIS = 30L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        textView = binding.text
        timestamp = binding.timestamp
        playerView = binding.playerView.apply {
            setKeepContentOnPlayerReset(true)
        }
        closeBtn = binding.toolbar.closeBtn
        canvasBtn = binding.options.canvasBtn
        timeBtn = binding.options.timeBtn
        doneBtn = binding.toolbar.doneBtn
        bufferProgressContainer = binding.bufferProgressContainer
        durationTextView = binding.totalDuration

        val videoUri = getVideoUri() ?: return finish()
        val viewModelFactory = VideoActivityViewModelFactory(videoUri, this)
        viewModel = ViewModelProvider(this, viewModelFactory)[VideoActivityViewModel::class.java]

        videoConfigurationDialogFragment =
            (supportFragmentManager.findFragmentByTag(VideoConfigurationDialogFragment.TAG) as VideoConfigurationDialogFragment?)
                ?: VideoConfigurationDialogFragment.newInstance().apply {
                    this.videoConfigurationCallback = this@VideoActivity
                }
        videoTimestampDialogFragment =
            (supportFragmentManager.findFragmentByTag(VideoTimestampDialogFragment.TAG) as VideoTimestampDialogFragment?)
                ?: VideoTimestampDialogFragment.newInstance().apply {
                    this.videoTimestampCallback = this@VideoActivity
                }

        progressDialogFragment =
            (supportFragmentManager.findFragmentByTag(ProgressDialogFragment.TAG) as ProgressDialogFragment?)
                ?: ProgressDialogFragment.newInstance()

        videoTrimManager = VideoTrimManager.getInstance(this)
        videoTrimManager.addTrimCallback(this)

        videoSelectionView = binding.videoSelection.apply {
            selectionCallback = this@VideoActivity
        }

        binding.playerContainer.setOnClickListener { togglePlayState() }

        binding.playBtn.setOnClickListener { playVideo() }

        viewModel.getTimestampDifferenceLiveData().observe(this) {
            durationTextView.text = it
        }

        viewModel.getTimestampLiveData().observe(this) {
            timestamp.text = it
        }

        viewModel.image.observe(this) {
            if (it != null) {
                binding.playerBackground.updateBitmap(it)
            } else binding.playerBackground.resetBitmap()
        }

        viewModel.getSelectedDimensionsLiveData().observe(this) {
            updateBackgroundFrame()
        }

        val positions = viewModel.getMarkerPositions()
        videoSelectionView.updateMarkers(positions.first, positions.second)

        doneBtn.setOnClickListener { showConfigurationDialog() }
        closeBtn.setOnClickListener { onBackPressed() }
        timeBtn.setOnClickListener { openTimestampFragment() }
        canvasBtn.setOnClickListener { showVideoCanvasFragment() }
    }

    override fun togglePlayState() {
        player?.let {
            if (it.isPlaying) {
                pauseVideo()
            } else {
                playVideo()
            }
        }
    }

    private fun getVideoCanvasFragment(): VideoCanvasFragment? {
        return supportFragmentManager.findFragmentByTag(VideoCanvasFragment.TAG) as VideoCanvasFragment?
    }

    private fun initVideoCanvasFragment(): VideoCanvasFragment {
        return VideoCanvasFragment().apply {
            arguments = Bundle().apply {
                putSerializable("video-canvas", viewModel.getSelectedDimensionsLiveData().value)
                putInt("blurFactor", viewModel.blurFactor)
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        binding.playBtn.isVisible = !isPlaying
        videoActivityCallbacks.forEach { it.onIsPlaying(isPlaying) }
    }

    private fun showVideoCanvasFragment() {
        val fragment = getVideoCanvasFragment() ?: initVideoCanvasFragment()
        supportFragmentManager.commit(true) {
            setCustomAnimations(R.anim.slide_in, R.anim.slide_out, R.anim.fade_in, R.anim.fade_out)
            replace(R.id.fragment_container_view, fragment, VideoCanvasFragment.TAG)
            addToBackStack(VideoCanvasFragment.TAG)
        }
    }

    private fun initFFMPEGListener() {
        FFmpegKitConfig.enableStatisticsCallback {
            val duration = viewModel.getTotalDurationByIndex()
            val progress = String.format(
                "%.2f",
                ((it.time.toFloat() / duration.toFloat()) * 100)
            )
            runOnUiThread {
                progressDialogFragment.setProgressTitle(
                    viewModel.trimProgress.first,
                    viewModel.trimProgress.second
                )
                progressDialogFragment.setProgressText(progress)
            }
        }
    }

    private fun showConfigurationDialog() {
        AndroidUtilities.showFragment(supportFragmentManager, videoConfigurationDialogFragment)
    }

    override fun playVideo() {
        player?.let {
            it.seekTo(viewModel.currentPosition)
            it.play()
        }
    }

    override fun pauseVideo() {
        player?.let {
            viewModel.currentPosition = it.currentPosition
            it.pause()
        }
    }

    private fun getVideoUri(): String? {
        val externalUri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri?
        return externalUri?.toString() ?: intent.getStringExtra("uri")
    }

    private fun initPlayer() {
        if (player == null) {
            player = ExoPlayer.Builder(this).build().also {
                playerView.player = it
                val mediaItem = MediaItem.fromUri(viewModel.uri)
                it.setMediaItem(mediaItem)
//            it.setVideoTextureView(textureView)
                it.seekTo(viewModel.currentPosition)
                it.playWhenReady = viewModel.isPlaying
                it.prepare()
                it.addListener(this)
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initFFMPEGListener()
            handler.postDelayed(playerRunnable, 0)
            initPlayer()
        }
    }

    public override fun onResume() {
        super.onResume()
        if ((Util.SDK_INT < 24 || player == null)) {
            initFFMPEGListener()
            handler.postDelayed(playerRunnable, 0)
            initPlayer()
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            handler.removeCallbacks(playerRunnable)
            releasePlayer()
        }
    }


    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            handler.removeCallbacks(playerRunnable)
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        player?.run {
//            this.clearVideoTextureView(textureView)
            viewModel.isPlaying = this.isPlaying
            viewModel.currentPosition = this.currentPosition
            this.removeListener(this@VideoActivity)
            release()
        }
        player = null
    }

    override fun onSelectionMoved(startX: Float, endX: Float) {
        viewModel.setVideoTimestamps(startX, endX)
    }

    override fun onSelectionStarted(startX: Float, endX: Float) {
        viewModel.setVideoTimestamps(startX, endX)
        player?.let {
            viewModel.isPlaying = it.isPlaying
            pauseVideo()
        }
    }

    override fun onSelectionEnded(startX: Float, endX: Float) {
        player?.let {
            it.seekTo(viewModel.convertToTimestamp(startX))
            viewModel.currentPosition = viewModel.getStartTime()
            if (viewModel.isPlaying) {
                playVideo()
            }
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        bufferProgressContainer.isVisible = playbackState == ExoPlayer.STATE_BUFFERING
        when (playbackState) {
            ExoPlayer.STATE_READY -> {
                updateVideoProgressMarker(player?.currentPosition ?: 0)
            }

        }
    }

    private fun checkPlayerProgress() {
        player?.let {
            val position = it.currentPosition
            updateVideoProgressMarker(position)
            if (position >= viewModel.getEndTime() - DELAY_MILLIS) {
                viewModel.currentPosition = viewModel.getStartTime()
                if (it.isPlaying) {
                    it.seekTo(viewModel.getStartTime())
                }
                it.pause()
            }
            videoActivityCallbacks.forEach { it.onPlayerProgress(position) }
        }
        updateBackgroundFrame()
    }

    private fun updateVideoProgressMarker(value: Long) {
        videoSelectionView.updateProgressMarkerPosition(viewModel.getProgressMarkerPosition(value))
    }

    @SuppressLint("RestrictedApi")
    override fun onFinish(splitTime: Long, quality: VideoQuality, format: VideoFormat) {
        viewModel.splitTime = splitTime
        AndroidUtilities.dismissFragment(videoConfigurationDialogFragment)
        val videoSize = player?.videoSize!!
        val selectedDimension = viewModel.getSelectedDimensions()
        val dimension = Dimension(videoSize.width.toFloat(), videoSize.height.toFloat())

        val workData = Data.Builder().apply {
            val videoTrimConfig = VideoTrimConfigModel(
                viewModel.uri,
                viewModel.getStartTime(),
                viewModel.getEndTime(),
                viewModel.splitTime,
                quality.name,
                format.extension
            )

            val canvasConfig: VideoCanvasConfigModel? = if (viewModel.shouldApplyBlurFilter()) {
                VideoCanvasConfigModel(
                    selectedDimension.width,
                    selectedDimension.height,
                    dimension.width,
                    dimension.height,
                    viewModel.blurFactor
                )
            } else null

            val editConfigModel =
                VideoEditConfigModel(trimConfig = videoTrimConfig, canvasConfig = canvasConfig)
            putString("config", editConfigModel.serialize())
        }.build()

        val workRequest = OneTimeWorkRequestBuilder<VideoTrimWorkManager>()
            .addTag(VideoTrimWorkManager.TAG).setInputData(workData)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).build()
        WorkManager.getInstance(this)
            .enqueueUniqueWork(VideoTrimWorkManager.TAG, ExistingWorkPolicy.KEEP, workRequest)
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        AndroidUtilities.showSnackBar(playerView, "Could not play this video")
    }

    override fun onTrimStarted(index: Int, total: Int) {
        runOnUiThread {
            viewModel.trimProgress = Pair(index, total)
            if (!supportFragmentManager.isDestroyed) {
                AndroidUtilities.showFragment(supportFragmentManager, progressDialogFragment)
            }
        }
    }

    override fun onTrimEnded(uris: List<Uri>) {
        lifecycleScope.launchWhenResumed {
            AndroidUtilities.dismissFragment(progressDialogFragment)
            AndroidUtilities.openShareIntent(ArrayList(uris), this@VideoActivity)
        }
    }

    private fun openTimestampFragment() {
        val start = viewModel.getStartTime()
        val end = viewModel.getEndTime()
        videoTimestampDialogFragment.setData(start, end)
        AndroidUtilities.showFragment(supportFragmentManager, videoTimestampDialogFragment)
    }

    override fun onSumbit(start: Long, end: Long) {
        AndroidUtilities.dismissFragment(videoTimestampDialogFragment)
        val duration = viewModel.getDuration()
        val _start = start.toFloat() / duration
        val _end = min(1.0f, end.toFloat() / duration)
        videoSelectionView.updateMarkers(_start, _end)
    }

    override fun registerVideoActivityCallback(videoPlayerCallback: VideoPlayerCallback) {
        videoActivityCallbacks.add(videoPlayerCallback)
    }

    override fun removeVideoActivityCallback(videoPlayerCallback: VideoPlayerCallback) {
        videoActivityCallbacks.remove(videoPlayerCallback)
    }

    override fun closeFragment() {
        getVideoCanvasFragment()?.let {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.remove(it)
            transaction.commitAllowingStateLoss()
            supportFragmentManager.popBackStack()
        }
    }

    override fun setVideoFrameProperties(videoCanvasModel: VideoCanvasModel, blurFactor: Int) {
        viewModel.setSelectedDimension(videoCanvasModel)
        viewModel.blurFactor = blurFactor
        closeFragment()
    }

    override fun onBlurSeekStarted() {
        pauseVideo()
    }

    override fun onBlurSeekEnded() {
        playVideo()
    }

    override fun getPlayer(): Player? {
        return player
    }

    override fun restorePlayerView() {
        playerView.player = null
        playerView.player = player
    }

    private fun updateBackgroundFrame() {
        (playerView.videoSurfaceView as TextureView?)?.bitmap?.let {
            viewModel.blurFrame(it)
        }
    }

}