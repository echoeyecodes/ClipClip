package com.echoeyecodes.clipclip.fragments.dialogfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.echoeyecodes.clipclip.R
import com.echoeyecodes.clipclip.callbacks.VideoTimestampCallback
import com.echoeyecodes.clipclip.databinding.FragmentVideoTimestampBinding
import com.echoeyecodes.clipclip.utils.AndroidUtilities
import com.echoeyecodes.clipclip.utils.formatDigitsToLong
import com.echoeyecodes.clipclip.utils.formatTimeToDigits
import com.echoeyecodes.clipclip.viewmodels.VideoTimestampViewModel
import com.vicmikhailau.maskededittext.MaskedEditText
import kotlin.math.max
import kotlin.math.min

class VideoTimestampDialogFragment : BaseDialogFragment() {
    private lateinit var binding: FragmentVideoTimestampBinding
    private lateinit var startEditText: MaskedEditText
    private lateinit var endEditText: MaskedEditText
    private lateinit var doneBtn: View
    var videoTimestampCallback: VideoTimestampCallback? = null
    private lateinit var viewModel: VideoTimestampViewModel

    companion object {
        const val TAG = "VIDEO_TIMESTAMP_DIALOG_FRAGMENT"
        fun newInstance() = VideoTimestampDialogFragment()
    }

    fun setData(startTime: Long, endTime: Long) {
        arguments = Bundle().apply {
            putLong("start", startTime)
            putLong("end", endTime)
        }
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
        val view = inflater.inflate(R.layout.fragment_video_timestamp, container, false)
        binding = FragmentVideoTimestampBinding.bind(view)
        startEditText = binding.startField
        endEditText = binding.endField
        doneBtn = binding.doneBtn
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[VideoTimestampViewModel::class.java]

        val startTime = arguments?.getLong("start") ?: 0L
        val endTime = arguments?.getLong("end") ?: 0L

        doneBtn.setOnClickListener { sendTimestamps() }
        startEditText.doOnTextChanged { text, start, before, count ->
            viewModel.setStartTime(text.toString())
        }
        endEditText.doOnTextChanged { text, start, before, count ->
            viewModel.setEndTime(text.toString())
        }
        viewModel.getValidStatus().observe(this) {
            doneBtn.isEnabled = it
        }

        startEditText.setText(startTime.formatTimeToDigits())
        endEditText.setText(endTime.formatTimeToDigits())
    }

    private fun sendTimestamps() {
        var start = startEditText.text.toString().formatDigitsToLong()
        var end = endEditText.text.toString().formatDigitsToLong()

        if (start > end) {
            end = start
        } else if (end < start) {
            start = end
        }
        videoTimestampCallback?.onSumbit(start, end)
        dismiss()
    }

}