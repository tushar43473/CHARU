package com.app.clipsteronline.upload.reelsediting.activity

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.clipsteronline.upload.reelsediting.R
import com.app.clipsteronline.upload.reelsediting.databinding.ReelsEditingActivityBinding

class ReelsEditingActivity : AppCompatActivity() {

    private lateinit var binding: ReelsEditingActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set to fullscreen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        // Hide action bar
        supportActionBar?.hide()
        
        // Use view binding
        binding = ReelsEditingActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Set status bar and nav bar colors for Android 5.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = getColor(R.color.dark_bg)
            window.navigationBarColor = getColor(R.color.dark_bg)
        }
        
        // Setup UI
        setupUI()
    }

    private fun setupUI() {
        // Back button click listener
        binding.backButton.setOnClickListener {
            finish()
        }
        
        // Music button
        binding.musicButton.setOnClickListener {
            // TODO: Implement music selection
        }
        
        // Text button
        binding.textButton.setOnClickListener {
            // TODO: Implement text editing
        }
        
        // Effects button
        binding.effectsButton.setOnClickListener {
            // TODO: Implement effects
        }
        
        // Export button
        binding.exportButton.setOnClickListener {
            // TODO: Implement export
        }
    }
}
