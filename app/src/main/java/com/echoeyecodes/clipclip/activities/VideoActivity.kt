package com.echoeyecodes.clipclip.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.echoeyecodes.clipclip.customviews.videoselectionview.VideoSelectionCallback
import com.echoeyecodes.clipclip.customviews.videoselectionview.VideoSelectionGravity
import com.echoeyecodes.clipclip.customviews.videoselectionview.VideoSelectionView
import com.echoeyecodes.clipclip.databinding.ActivityVideoSelectionBinding
import com.echoeyecodes.clipclip.viewmodels.VideoActivityViewModel
import com.echoeyecodes.clipclip.viewmodels.VideoActivityViewModelFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Util

class VideoActivity : AppCompatActivity(), VideoSelectionCallback, Player.Listener {
    private val binding by lazy { ActivityVideoSelectionBinding.inflate(layoutInflater) }
    private lateinit var textView: TextView
    private lateinit var timestamp: TextView
    private lateinit var durationTextView: TextView
    private lateinit var videoSelectionView: VideoSelectionView
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var viewModel: VideoActivityViewModel
    private val handler by lazy { Handler(mainLooper) }

    private val playerRunnable = object : Runnable {
        override fun run() {
            checkPlayerProgress()
            handler.postDelayed(this, 300)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        textView = binding.text
        timestamp = binding.timestamp
        playerView = binding.playerView
        durationTextView = binding.totalDuration

        val duration = intent.getLongExtra("duration", 0L)

        val viewModelFactory = VideoActivityViewModelFactory(duration, this)
        viewModel = ViewModelProvider(this, viewModelFactory)[VideoActivityViewModel::class.java]

        videoSelectionView = binding.videoSelection.apply {
            selectionCallback = this@VideoActivity
        }

        binding.root.setOnClickListener {
            player?.let {
                if (it.isPlaying) {
                    pauseVideo()
                } else {
                    playVideo()
                }
            }
        }

        viewModel.getTimestampDifferenceLiveData().observe(this, {
            durationTextView.text = it
        })

        viewModel.getTimestampLiveData().observe(this, {
            timestamp.text = it
        })

        val positions = viewModel.getMarkerPositions()
        videoSelectionView.updateMarkerPosition(positions.first, positions.second)
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

    private fun initPlayer() {
        val uri = intent.getStringExtra("uri") ?: return finish()

        player = ExoPlayer.Builder(this).build().also {
            playerView.player = it
            val mediaItem = MediaItem.fromUri(uri)
            it.setMediaItem(mediaItem)
            it.seekTo(viewModel.currentPosition)
            it.playWhenReady = true
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
            playWhenReady = this.playWhenReady
            viewModel.currentPosition = this.currentPosition
            this.removeListener(this@VideoActivity)
            release()
        }
        player = null
    }

    @SuppressLint("SetTextI18n")
    override fun onSelectionMoved(startX: Float, endX: Float) {
        textView.text = "Start: ${startX.toInt()}% \n End ${endX.toInt()}%"
        viewModel.setVideoTimestamps(startX, endX)
    }

    override fun onSelectionStarted(gravity: VideoSelectionGravity, startX: Float, endX: Float) {
        viewModel.setVideoTimestamps(startX, endX)
        player?.let {
            viewModel.isPlaying = it.isPlaying
            pauseVideo()
        }
    }

    override fun onSelectionEnded(gravity: VideoSelectionGravity, startX: Float, endX: Float) {
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
        when (playbackState) {
            ExoPlayer.STATE_READY -> {
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
            if (position >= viewModel.getEndTime()) {
                viewModel.currentPosition = viewModel.getStartTime()
                it.pause()
            }
        }
    }
}