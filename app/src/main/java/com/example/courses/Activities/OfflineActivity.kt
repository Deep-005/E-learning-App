package com.example.courses.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.courses.R
import com.example.courses.databinding.ActivityOfflineBinding

class OfflineActivity : AppCompatActivity() {

    private lateinit var binding:ActivityOfflineBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflineBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        binding.offlineAnime.playAnimation()  // Start the animation when the activity is visible
    }

    override fun onStop() {
        super.onStop()
        binding.offlineAnime.cancelAnimation()  // Stop the animation when the activity is not visible
    }
}