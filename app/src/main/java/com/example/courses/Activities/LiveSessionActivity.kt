package com.example.courses.Activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.courses.R
import com.example.courses.databinding.ActivityCertificatesBinding
import com.example.courses.databinding.ActivityLiveSessionBinding

class LiveSessionActivity : AppCompatActivity() {

    lateinit var binding: ActivityLiveSessionBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveSessionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val webView: WebView = findViewById(R.id.live_class)
        val link = intent.getStringExtra("link")

        // Enable JavaScript
        webView.settings.javaScriptEnabled = true

        // Set up WebViewClient with handling for unknown URI schemes
        webView.webViewClient = object : WebViewClient() {
            @SuppressLint("QueryPermissionsNeeded")
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                url?.let {
                    Log.d("WebViewDebug", "Handling URL: $it")
                    return when {
                        it.startsWith("http://") || it.startsWith("https://") -> {
                            view.loadUrl(it)
                            true
                        }
                        it.startsWith("intent://") -> {
                            try {
                                val intent = Intent.parseUri(it, Intent.URI_INTENT_SCHEME)
                                // Check if there's an app to handle this intent
                                if (intent.resolveActivity(packageManager) != null) {
                                    startActivity(intent)
                                } else {
                                    // Use fallback URL if provided
                                    val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                                    if (fallbackUrl != null) {
                                        Log.d("WebViewDebug", "Fallback URL: $fallbackUrl")
                                        view.loadUrl(fallbackUrl)
                                    } else {
                                        Toast.makeText(
                                            this@LiveSessionActivity,
                                            "No app found to handle this link and no fallback URL provided.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("WebViewDebug", "Error handling intent URL: ${e.message}")
                                Toast.makeText(
                                    this@LiveSessionActivity,
                                    "Error handling intent URL",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            true
                        }
                        else -> {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                                startActivity(intent)
                            } catch (e: Exception) {
                                Log.e("WebViewDebug", "Error handling unknown scheme: ${e.message}")
                                Toast.makeText(this@LiveSessionActivity, "Unable to open link", Toast.LENGTH_SHORT).show()
                            }
                            true
                        }
                    }
                }
                return false
            }


        }

        // Load the link from the intent
        link?.let { webView.loadUrl(it) }
    }
}
