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
import com.echoeyecodes.clipclip.utils.VideoModelItemCallback

class VideoAdapter(private val callback: VideoAdapterCallback) :
    ListAdapter<VideoModel, VideoAdapter.VideoAdapterViewHolder>(VideoModelItemCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoAdapterViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_video_item, parent, false)
        return VideoAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoAdapterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VideoAdapterViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val layoutBinding = LayoutVideoItemBinding.bind(view)
        private val imageView = layoutBinding.image

        fun bind(model: VideoModel) {
            Glide.with(view).load(model.getVideoUri()).into(imageView)
            view.setOnClickListener { callback.onVideoSelected(model) }
        }
    }
}