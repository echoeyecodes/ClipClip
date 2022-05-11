package com.echoeyecodes.clipclip.fragments.videofragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
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
import com.google.android.exoplayer2.ui.PlayerView

class VideoCanvasFragment : Fragment(), VideoPlayerCallback, VideoCanvasAdapterCallback {
    private lateinit var playerView: PlayerView
    private lateinit var playerBackground: VideoFrameView
    private lateinit var binding: FragmentVideoCanvasBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var closeBtn: View
    private lateinit var doneBtn: View
    private val viewModel by lazy { ViewModelProvider(this)[VideoCanvasViewModel::class.java] }
    var videoActivityCallback: VideoActivityCallback? = null

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

        closeBtn.setOnClickListener { videoActivityCallback?.closeFragment() }
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

        viewModel.videoDimensions.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        playerView.player = videoActivityCallback?.getPlayer()
        playerView.setAspectRatioListener { _, _, _ ->
            updateBackgroundFrame()
        }

        viewModel.image.observe(viewLifecycleOwner) {
            if (it != null) {
                playerBackground.updateBitmap(it)
            } else playerBackground.resetBitmap()
        }
    }

    override fun onResume() {
        super.onResume()
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
        playerView.player = null
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

    override fun onCanvasItemSelected(model: VideoCanvasModel) {
        viewModel.setSelectedDimension(model)
    }
}