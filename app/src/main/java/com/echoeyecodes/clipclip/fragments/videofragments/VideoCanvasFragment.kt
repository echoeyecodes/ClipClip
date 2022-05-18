package com.echoeyecodes.clipclip.fragments.videofragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.echoeyecodes.clipclip.R
import com.echoeyecodes.clipclip.adapters.VideoCanvasAdapter
import com.echoeyecodes.clipclip.callbacks.VideoActivityCallback
import com.echoeyecodes.clipclip.callbacks.VideoCanvasAdapterCallback
import com.echoeyecodes.clipclip.callbacks.VideoPlayerCallback
import com.echoeyecodes.clipclip.customviews.videoview.VideoFrameView
import com.echoeyecodes.clipclip.databinding.FragmentVideoCanvasBinding
import com.echoeyecodes.clipclip.models.VideoCanvasModel
import com.echoeyecodes.clipclip.utils.AndroidUtilities
import com.echoeyecodes.clipclip.utils.CustomItemDecoration
import com.echoeyecodes.clipclip.viewmodels.VideoCanvasViewModel
import com.echoeyecodes.clipclip.viewmodels.VideoCanvasViewModelFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView

class VideoCanvasFragment : Fragment(), VideoPlayerCallback, VideoCanvasAdapterCallback,
    SeekBar.OnSeekBarChangeListener {
    private lateinit var playerView: PlayerView
    private lateinit var playerBackground: VideoFrameView
    private lateinit var binding: FragmentVideoCanvasBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var closeBtn: View
    private lateinit var doneBtn: View
    private lateinit var seekBar: SeekBar
    private lateinit var viewModel: VideoCanvasViewModel
    private var videoActivityCallback: VideoActivityCallback? = null
    private var player: Player? = null

    companion object {
        const val TAG = "VIDEO_CANVAS_FRAGMENT"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_video_canvas, container, false)
        binding = FragmentVideoCanvasBinding.bind(view)
        playerView = binding.playerView
        recyclerView = binding.recyclerView
        playerBackground = binding.playerBackground
        closeBtn = binding.toolbar.closeBtn
        doneBtn = binding.toolbar.doneBtn
        seekBar = binding.seekBar

        val videoCanvasModel = arguments?.getSerializable("video-canvas") as VideoCanvasModel?
        val viewModelFactory = VideoCanvasViewModelFactory(videoCanvasModel, requireContext())
        viewModel = ViewModelProvider(this, viewModelFactory)[VideoCanvasViewModel::class.java]
        seekBar.progress = viewModel.blurFactor

        closeBtn.setOnClickListener { videoActivityCallback?.closeFragment() }
        doneBtn.setOnClickListener { setVideoBackground() }
        seekBar.setOnSeekBarChangeListener(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val adapter = VideoCanvasAdapter(this)
        recyclerView.layoutManager = layoutManager
        val itemDecoration = CustomItemDecoration(10, 5)
        recyclerView.addItemDecoration(itemDecoration)
        recyclerView.adapter = adapter
        playerView.setKeepContentOnPlayerReset(true)

        viewModel.videoDimensions.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.image.observe(viewLifecycleOwner) {
            if (it != null) {
                playerBackground.updateBitmap(it)
            } else {
                playerBackground.resetBitmap()
            }
        }
        viewModel.getSelectedDimensionsLiveData().observe(viewLifecycleOwner) {
            updateBackgroundFrame()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        videoActivityCallback = context as VideoActivityCallback
    }

    override fun onResume() {
        super.onResume()
        player = videoActivityCallback?.getPlayer()
        playerView.player = player
        videoActivityCallback?.registerVideoActivityCallback(this)
    }

    override fun onPause() {
        super.onPause()
        videoActivityCallback?.removeVideoActivityCallback(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        videoActivityCallback?.removeVideoActivityCallback(this)
        restorePlayerView()
    }

    private fun restorePlayerView() {
        videoActivityCallback?.restorePlayerView()
    }

    override fun onPlayerProgress(timestamp: Long) {
        updateBackgroundFrame()
    }

    private fun updateBackgroundFrame() {
        (playerView.videoSurfaceView as TextureView?)?.bitmap?.let {
            viewModel.blurFrame(it)
        }
    }

    private fun setVideoBackground() {
        videoActivityCallback?.setVideoBackground(viewModel.getSelectedDimensionsLiveData().value!!)
    }

    override fun onCanvasItemSelected(model: VideoCanvasModel) {
        viewModel.setSelectedDimension(model)
    }

    override fun onProgressChanged(p0: SeekBar, p1: Int, p2: Boolean) {
        viewModel.blurFactor = (p1)
    }

    override fun onStartTrackingTouch(p0: SeekBar) {
        videoActivityCallback?.onBlurSeekStarted()
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
        videoActivityCallback?.onBlurSeekEnded()
    }
}