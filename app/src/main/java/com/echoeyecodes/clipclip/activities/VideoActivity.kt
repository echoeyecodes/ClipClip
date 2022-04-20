package com.echoeyecodes.clipclip.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.echoeyecodes.clipclip.callbacks.VideoConfigurationCallback
import com.echoeyecodes.clipclip.callbacks.VideoTimestampCallback
import com.echoeyecodes.clipclip.callbacks.VideoTrimCallback
import com.echoeyecodes.clipclip.customviews.videoselectionview.VideoSelectionCallback
import com.echoeyecodes.clipclip.customviews.videoselectionview.VideoSelectionView
import com.echoeyecodes.clipclip.databinding.ActivityVideoSelectionBinding
import com.echoeyecodes.clipclip.fragments.dialogfragments.ProgressDialogFragment
import com.echoeyecodes.clipclip.fragments.dialogfragments.VideoConfigurationDialogFragment
import com.echoeyecodes.clipclip.fragments.dialogfragments.VideoTimestampDialogFragment
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
import kotlin.math.max
import kotlin.math.min

class VideoActivity : AppCompatActivity(), VideoSelectionCallback, Player.Listener,
    VideoConfigurationCallback, VideoTrimCallback, VideoTimestampCallback {
    private lateinit var videoTrimManager: VideoTrimManager
    private val binding by lazy { ActivityVideoSelectionBinding.inflate(layoutInflater) }
    private lateinit var textView: TextView
    private lateinit var timestamp: TextView
    private lateinit var bufferProgressContainer: View
    private lateinit var durationTextView: TextView
    private lateinit var doneBtn: MaterialButton
    private lateinit var timeBtn: View
    private lateinit var closeBtn: View
    private lateinit var videoSelectionView: VideoSelectionView
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var viewModel: VideoActivityViewModel
    private lateinit var progressDialogFragment: ProgressDialogFragment
    private lateinit var videoConfigurationDialogFragment: VideoConfigurationDialogFragment
    private lateinit var videoTimestampDialogFragment: VideoTimestampDialogFragment
    private val handler by lazy { Handler(mainLooper) }

    private val playerRunnable = object : Runnable {
        override fun run() {
            checkPlayerProgress()
            handler.postDelayed(this, 30)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        textView = binding.text
        timestamp = binding.timestamp
        playerView = binding.playerView
        closeBtn = binding.toolbar.closeBtn
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

        binding.playerContainer.setOnClickListener {
            player?.let {
                if (it.isPlaying) {
                    pauseVideo()
                } else {
                    playVideo()
                }
            }
        }

        viewModel.getTimestampDifferenceLiveData().observe(this) {
            durationTextView.text = it
        }

        viewModel.getTimestampLiveData().observe(this) {
            timestamp.text = it
        }

        val positions = viewModel.getMarkerPositions()
        videoSelectionView.updateMarkers(positions.first, positions.second)

        doneBtn.setOnClickListener { showConfigurationDialog() }
        closeBtn.setOnClickListener { onBackPressed() }
        timeBtn.setOnClickListener { openTimestampFragment() }
    }

    private fun initFFMPEGListener() {
        FFmpegKitConfig.enableStatisticsCallback {
            val duration = viewModel.getTotalDurationByIndex()
            val progress = String.format(
                "%.2f",
                ((it.time.toFloat() / 1000.toFloat() / duration.toFloat()) * 100)
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

    private fun playVideo() {
        player?.let {
            it.seekTo(viewModel.currentPosition)
            it.play()
        }
    }

    private fun pauseVideo() {
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
        player = ExoPlayer.Builder(this).build().also {
            playerView.player = it
            val mediaItem = MediaItem.fromUri(viewModel.uri)
            it.setMediaItem(mediaItem)
            it.seekTo(viewModel.currentPosition)
            it.playWhenReady = viewModel.isPlaying
            it.prepare()
            it.addListener(this)
        }
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initPlayer()
        }
    }

    public override fun onResume() {
        super.onResume()
        initFFMPEGListener()
        if ((Util.SDK_INT < 24 || player == null)) {
            initPlayer()
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }


    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        player?.run {
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
//                val duration = player?.duration ?: 0
//                viewModel.duration = duration
            }

        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        if (isPlaying) {
            handler.postDelayed(playerRunnable, 0)
        } else {
            handler.removeCallbacks(playerRunnable)
        }
    }

    private fun checkPlayerProgress() {
        player?.let {
            val position = it.currentPosition
            updateVideoProgressMarker(position)
            if (position >= viewModel.getEndTime()) {
                viewModel.currentPosition = viewModel.getStartTime()
                it.pause()
            }
        }
    }

    private fun updateVideoProgressMarker(value: Long) {
        videoSelectionView.updateProgressMarkerPosition(viewModel.getProgressMarkerPosition(value))
    }

    override fun onFinish(splitTime: Int, quality: VideoQuality, format: VideoFormat) {
        viewModel.splitTime = splitTime
        AndroidUtilities.dismissFragment(videoConfigurationDialogFragment)
        val workData = Data.Builder().apply {
            putString("videoUri", viewModel.uri)
            putLong("startTime", viewModel.getStartTime())
            putLong("endTime", viewModel.getEndTime())
            putInt("splitTime", viewModel.splitTime)
            putString("quality", quality.qName)
            putString("format", format.extension)
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
}