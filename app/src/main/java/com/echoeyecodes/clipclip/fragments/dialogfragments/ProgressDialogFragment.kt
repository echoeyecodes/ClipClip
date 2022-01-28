package com.echoeyecodes.clipclip.fragments.dialogfragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.echoeyecodes.clipclip.R

class ProgressDialogFragment : BaseDialogFragment() {

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
        progressTitleTextView = view.findViewById(R.id.progress_title)
        return view
    }

    @SuppressLint("SetTextI18n")
    fun setProgressText(value: String) {
        if (isVisible) {
            progressTextView.text = "Current Progress: $value%"
        }
    }

    @SuppressLint("SetTextI18n")
    fun setProgressTitle(index: Int, total:Int) {
        if (isVisible) {
            progressTitleTextView.text = "Trimming Video ($index of $total)"
        }
    }
}