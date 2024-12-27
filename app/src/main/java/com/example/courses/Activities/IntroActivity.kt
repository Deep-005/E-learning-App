package com.example.courses.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.courses.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {

    lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use Handler to start MainActivity after a delay
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@IntroActivity, EntryActivity::class.java)
            startActivity(intent)
            finish()
        }, 1500)

    }
}