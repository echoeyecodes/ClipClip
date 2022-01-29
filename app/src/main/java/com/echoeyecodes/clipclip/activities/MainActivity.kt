package com.echoeyecodes.clipclip.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.echoeyecodes.clipclip.R
import com.echoeyecodes.clipclip.databinding.ActivityMainBinding
import com.echoeyecodes.clipclip.utils.ActivityUtil

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var readyBtn: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        readyBtn = binding.readyBtn
        updateStatusAndNavColor()

        readyBtn.setOnClickListener { ActivityUtil.startSelectVideoActivity(this) }
    }

    override fun onResume() {
        super.onResume()
        updateStatusAndNavColor()
    }

    @SuppressLint("ResourceAsColor")
    private fun updateStatusAndNavColor() {
        window.statusBarColor = resources.getColor(R.color.colorPrimary, null)
        window.navigationBarColor = resources.getColor(R.color.colorPrimary, null)
    }
}