package com.echoeyecodes.clipclip.fragments.dialogfragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.work.WorkManager
import com.arthenica.ffmpegkit.FFmpegKit
import com.echoeyecodes.clipclip.R
import com.echoeyecodes.clipclip.workmanager.VideoTrimWorkManager

class ProgressDialogFragment : BaseDialogFragment() {

    private lateinit var cancelBtn: View
    private lateinit var progressTitleTextView: TextView
    private lateinit var progressTextView: TextView

    companion object {
        const val TAG = "PROGRESS_DIALOG_FRAGMENT"
        fun newInstance() = ProgressDialogFragment()
    }

    override fun getDialogTag(): String {
        return TAG
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
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
        val view = inflater.inflate(R.layout.fragment_progress_dialog, container, false)
        progressTextView = view.findViewById(R.id.progress_description)
        cancelBtn = view.findViewById(R.id.cancel_btn)
        progressTitleTextView = view.findViewById(R.id.progress_title)

        cancelBtn.setOnClickListener { terminateService() }
        return view
    }

    private fun terminateService() {
        FFmpegKit.cancel()
        WorkManager.getInstance(requireContext()).cancelAllWorkByTag(VideoTrimWorkManager.TAG)
    }

    @SuppressLint("SetTextI18n")
    fun setProgressText(value: String) {
        if (isVisible) {
            progressTextView.text = "Current Progress: $value%"
        }
    }

    @SuppressLint("SetTextI18n")
    fun setProgressTitle(index: Int, total: Int) {
        if (isVisible) {
            progressTitleTextView.text = "Trimming Video ($index of $total)"
        }
    }
}