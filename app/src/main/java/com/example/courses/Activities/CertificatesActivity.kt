package com.example.courses.Activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.courses.Adapters.CertificateAdapter
import com.example.courses.Data.Certificate
import com.example.courses.R
import com.example.courses.databinding.ActivityCertificatesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CertificatesActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityCertificatesBinding
    private lateinit var adapter: CertificateAdapter
    private var certificatesList: MutableList<Certificate> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCertificatesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        binding.backToUser .setOnClickListener {
            finish()
            overridePendingTransition(R.anim.no_animation, R.anim.slide_down)
        }

        setupRecyclerView()
        fetchCertificates()
    }

    private fun setupRecyclerView() {
        adapter = CertificateAdapter(certificatesList, this) // Pass the context
        binding.certificateRV.adapter = adapter
        binding.certificateRV.layoutManager = LinearLayoutManager(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchCertificates() {
        val userId = auth.currentUser
        val db = FirebaseFirestore.getInstance()

        db.collection("Users").document(userId.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    // Retrieve the certificates array from the document
                    val certificates = document.get("certificates") as? List<Map<String, String>> ?: emptyList()

                    // Clear the existing list to avoid duplication
                    certificatesList.clear()

                    // Iterate through the list of certificates
                    for (certificate in certificates) {
                        val imageUrl = certificate["imageUrl"] ?: ""
                        val downloadUrl = certificate["downloadUrl"] ?: ""
                        // Create a Certificate object and add it to the list
                        certificatesList.add(Certificate(imageUrl, downloadUrl))
                    }

                    // Check if the list is empty and update the UI
                    if (certificatesList.isEmpty()) {
                        binding.certificateRV.visibility = View.GONE
                        binding.noCertificate.visibility = View.VISIBLE
                    } else {
                        binding.certificateRV.visibility = View.VISIBLE
                        binding.noCertificate.visibility = View.GONE
                    }

                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Log.w("CertificatesActivity", "Error getting documents: ", exception)
                // In case of failure, show the noCertificate view
                binding.certificateRV.visibility = View.GONE
                binding.noCertificate.visibility = View.VISIBLE
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.no_animation, R.anim.slide_down)
    }
}