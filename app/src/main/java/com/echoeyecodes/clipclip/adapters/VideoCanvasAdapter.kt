package com.echoeyecodes.clipclip.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.echoeyecodes.clipclip.R
import com.echoeyecodes.clipclip.callbacks.VideoCanvasAdapterCallback
import com.echoeyecodes.clipclip.databinding.LayoutCanvasSizeBinding
import com.echoeyecodes.clipclip.models.VideoCanvasModel
import com.echoeyecodes.clipclip.utils.*

class VideoCanvasAdapter(private val callback: VideoCanvasAdapterCallback) :
    ListAdapter<VideoCanvasModel, VideoCanvasAdapter.VideoCanvasViewHolder>(
        VideoCanvasModelItemCallback()
    ) {

    companion object {
        const val UNSELECTED_LAYOUT = 0
        const val SELECTED_LAYOUT = 1
    }

    private val size = 80.convertToDp().toFloat()

    override fun getItemViewType(position: Int): Int {
        val model = getItem(position)
        return if (model.isSelected) {
            SELECTED_LAYOUT
        } else UNSELECTED_LAYOUT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoCanvasViewHolder {
        val layout = if (viewType == SELECTED_LAYOUT) {
            R.layout.layout_canvas_size_selected
        } else R.layout.layout_canvas_size
        val view =
            LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return VideoCanvasViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoCanvasViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VideoCanvasViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = LayoutCanvasSizeBinding.bind(view)
        private val cardView = binding.cardView
        private val textView = binding.textView

        @SuppressLint("SetTextI18n")
        fun bind(model: VideoCanvasModel) {
            if (model.width == 0.0f && model.height == 0.0f) {
                cardView.layoutParams.width = size.toInt()
                cardView.layoutParams.height = size.toInt()
                textView.text = "No Frame"
            } else {
                val dimension = Pair(model.width, model.height).getDimensions(size)
                cardView.layoutParams.width = dimension.first.toInt()
                cardView.layoutParams.height = dimension.second.toInt()
                textView.text = "${model.width.toInt()}:${model.height.toInt()}"
            }

            cardView.setOnClickListener { callback.onCanvasItemSelected(model) }
        }
    }
}