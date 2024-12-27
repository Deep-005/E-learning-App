package com.example.courses.Activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.courses.R
import com.example.courses.databinding.ActivityViewPdfactivityBinding
import com.github.barteksc.pdfviewer.PDFView
import java.net.URL
import kotlin.concurrent.thread

class ViewPDFActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewPdfactivityBinding
    private lateinit var pdfView: PDFView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewPdfactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.no_animation, R.anim.slide_down)
        }

        pdfView = findViewById(R.id.pdfView)

        // Get the PDF URL and name from the Intent
        val pdfUrl = intent.getStringExtra("pdf_url")
        val pdfName = intent.getStringExtra("pdf_name")

        binding.headingPDF.text = pdfName

        if (pdfUrl != null) {
            // Load PDF from the received URL
            loadPdfFromUrl(pdfUrl)
        } else {
            // Handle the error if no URL was passed
            Toast.makeText(this, "No PDF URL provided", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPdfFromUrl(pdfUrl: String) {
        thread {
            try {
                val pdfInputStream = URL(pdfUrl).openStream()
                runOnUiThread {
                    pdfView.fromStream(pdfInputStream).load()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Failed to load PDF", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}


