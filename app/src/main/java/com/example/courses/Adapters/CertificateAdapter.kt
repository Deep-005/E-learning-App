package com.example.courses.Adapters

import android.app.Activity
import android.content.Context
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.courses.Data.Certificate
import com.example.courses.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CertificateAdapter(
    private val certificates: List<Certificate>,
    private val context: Context // Pass context to the adapter
) : RecyclerView.Adapter<CertificateAdapter.CertificateViewHolder>() {

    class CertificateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val certificateImg: ImageView = itemView.findViewById(R.id.certificateImg)
        private val downloadCertificate: TextView = itemView.findViewById(R.id.downloadCertificate)

        fun bind(certificate: Certificate, context: Context) {
            // Load the image using Glide
            Glide.with(itemView.context)
                .load(certificate.imageUrl)
                .placeholder(R.drawable.live_class)
                .into(certificateImg)

            // Set click listener for the download button
            downloadCertificate.setOnClickListener {
                downloadFile(certificate.downloadUrl, context)
            }
        }

        private fun downloadFile(url: String, context: Context) {
            // Use OkHttp to download the file
            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("CertificateAdapter", "Download failed: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val inputStream = response.body?.byteStream()
                        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "certificate.pdf") // Change the file name and extension as needed
                        val outputStream = FileOutputStream(file)

                        inputStream?.use { input ->
                            outputStream.use { output ->
                                input.copyTo(output)
                            }
                        }

                        // Notify the user of the successful download
                        (context as Activity).runOnUiThread {
                            Toast.makeText(context, "Certificate downloaded to ${file.absolutePath}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Log.e("CertificateAdapter", "Download failed: ${response.message}")
                    }
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CertificateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.certificate_card, parent, false)
        return CertificateViewHolder(view)
    }

    override fun onBindViewHolder(holder: CertificateViewHolder, position: Int) {
        holder.bind(certificates[position], context)
    }

    override fun getItemCount(): Int {
        return certificates.size
    }
}