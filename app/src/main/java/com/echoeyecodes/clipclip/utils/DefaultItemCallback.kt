package com.echoeyecodes.clipclip.utils

import androidx.recyclerview.widget.DiffUtil
import com.echoeyecodes.clipclip.models.VideoCanvasModel
import com.echoeyecodes.clipclip.models.VideoModel

class DefaultItemCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}

class VideoModelItemCallback : DiffUtil.ItemCallback<VideoModel>() {
    override fun areItemsTheSame(oldItem: VideoModel, newItem: VideoModel): Boolean {
        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: VideoModel, newItem: VideoModel): Boolean {
        return oldItem == newItem
    }
}

class VideoCanvasModelItemCallback : DiffUtil.ItemCallback<VideoCanvasModel>() {
    override fun areItemsTheSame(oldItem: VideoCanvasModel, newItem: VideoCanvasModel): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: VideoCanvasModel, newItem: VideoCanvasModel): Boolean {
        return oldItem == newItem && oldItem.isSelected == newItem.isSelected
    }
}
