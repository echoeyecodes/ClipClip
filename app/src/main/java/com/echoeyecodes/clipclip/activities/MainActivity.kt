package com.echoeyecodes.clipclip.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
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

    private fun updateStatusAndNavColor() {
        val color = ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
        window.statusBarColor = color
        window.navigationBarColor = color
    }
}