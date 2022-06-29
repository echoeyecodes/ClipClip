package com.echoeyecodes.clipclip.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.echoeyecodes.clipclip.R
import com.echoeyecodes.clipclip.callbacks.VideoAdapterCallback
import com.echoeyecodes.clipclip.databinding.LayoutVideoItemBinding
import com.echoeyecodes.clipclip.models.VideoModel
import com.echoeyecodes.clipclip.utils.*

class VideoAdapter(private val callback: VideoAdapterCallback) :
    ListAdapter<VideoModel, VideoAdapter.VideoAdapterViewHolder>(VideoModelItemCallback()) {

    private val size = (getScreenSize().first / 4) - 6.convertToDp()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoAdapterViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_video_item, parent, false)
        val height = (size + (size * 0.1)).toInt()
        view.layoutParams.width = size
        view.layoutParams.height = height
        return VideoAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoAdapterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VideoAdapterViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val layoutBinding = LayoutVideoItemBinding.bind(view)
        private val imageView = layoutBinding.image
        private val timestampTextView = layoutBinding.timestamp

        fun bind(model: VideoModel) {
            model.videoUri?.let {
                Glide.with(view).load(it).override(size)
                    .into(imageView)
            }
            view.setOnClickListener { callback.onVideoSelected(model) }
            timestampTextView.text = model.duration.formatVideoTime()
        }
    }
}