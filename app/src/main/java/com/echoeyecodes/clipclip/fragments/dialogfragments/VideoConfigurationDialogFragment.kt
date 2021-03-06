package com.echoeyecodes.clipclip.fragments.dialogfragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.echoeyecodes.clipclip.R
import com.echoeyecodes.clipclip.callbacks.VideoConfigurationCallback
import com.echoeyecodes.clipclip.databinding.FragmentVideoConfigurationBinding
import com.echoeyecodes.clipclip.utils.VideoFormat
import com.echoeyecodes.clipclip.utils.VideoQuality
import com.echoeyecodes.clipclip.viewmodels.VideoConfigurationViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup

class VideoConfigurationDialogFragment : BaseDialogFragment(), SeekBar.OnSeekBarChangeListener {

    private lateinit var seekBarText: TextView
    private lateinit var chipGroup: ChipGroup
    private lateinit var seekbar: SeekBar
    private lateinit var radioGroup: RadioGroup
    private val viewModel by lazy { ViewModelProvider(this)[VideoConfigurationViewModel::class.java] }
    private lateinit var binding: FragmentVideoConfigurationBinding
    private lateinit var doneBtn: MaterialButton
    var videoConfigurationCallback: VideoConfigurationCallback? = null

    companion object {
        const val TAG = "VIDEO_CONFIGURATION_DIALOG_FRAGMENT"
        fun newInstance() = VideoConfigurationDialogFragment()
    }

    override fun getDialogTag(): String {
        return TAG
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomAlertDialog)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_video_configuration, container, false)
        binding = FragmentVideoConfigurationBinding.bind(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        doneBtn = binding.doneBtn
        seekbar = binding.seekBar
        radioGroup = binding.qualityRadioGroup
        seekBarText = binding.seekBarText
        chipGroup = binding.formatChipGroup

        doneBtn.setOnClickListener {
            videoConfigurationCallback?.onFinish(
                viewModel.splitTime,
                viewModel.quality,
                viewModel.format
            )
        }

        seekbar.setOnSeekBarChangeListener(this)
        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.mp3) {
                viewModel.format = (VideoFormat.MP3)
            } else {
                viewModel.format = (VideoFormat.MP4)
            }
        }

        seekbar.progress = (viewModel.splitTime / 1000).toInt()
        chipGroup.check(getCheckedFormat())
        radioGroup.check(getCheckedQuality())

        radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.very_low -> {
                    viewModel.quality = (VideoQuality.VERY_LOW)
                }
                R.id.low -> {
                    viewModel.quality = (VideoQuality.LOW)
                }
                R.id.medium -> {
                    viewModel.quality = (VideoQuality.MEDIUM)
                }
                R.id.high -> {
                    viewModel.quality = (VideoQuality.HIGH)
                }
                else -> {
                    viewModel.quality = (VideoQuality.NORMAL)
                }
            }
        }
    }

    private fun getCheckedQuality(): Int {
        return when (viewModel.quality) {
            VideoQuality.VERY_LOW -> {
                R.id.very_low
            }
            VideoQuality.LOW -> {
                R.id.low
            }
            VideoQuality.MEDIUM -> {
                R.id.medium
            }
            VideoQuality.HIGH -> {
                R.id.high
            }
            else -> {
                R.id.normal
            }
        }
    }

    private fun getCheckedFormat(): Int {
        return when (viewModel.format) {
            VideoFormat.MP3 -> {
                R.id.mp3
            }
            else -> {
                R.id.mp4
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onProgressChanged(p0: SeekBar, p1: Int, p2: Boolean) {
        viewModel.splitTime = (p1 * 1000L)
        seekBarText.text = "Video would be split to $p1 seconds for every chunk"
    }

    override fun onStartTrackingTouch(p0: SeekBar) {

    }

    override fun onStopTrackingTouch(p0: SeekBar) {

    }

}