package com.example.courses.Activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.courses.databinding.ActivityEntryBinding

class EntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEntryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}