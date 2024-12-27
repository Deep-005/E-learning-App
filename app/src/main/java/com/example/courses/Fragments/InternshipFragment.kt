package com.example.courses.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.courses.Adapters.InternshipAdapter
import com.example.courses.Data.Internship
import com.example.courses.R
import com.google.firebase.firestore.FirebaseFirestore

class InternshipFragment : Fragment() {

    private lateinit var internshipAdapter: InternshipAdapter
    private val internshipList = mutableListOf<Internship>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_internship, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.internRV)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        internshipAdapter = InternshipAdapter(internshipList) // Pass context here
        recyclerView.adapter = internshipAdapter

        fetchInternships()

        // Back button setup
        val back = view.findViewById<ImageButton>(R.id.backToDashFromInternshipPage)
        back.setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchInternships() {
        val db = FirebaseFirestore.getInstance()
        db.collection("InternShips")
            .get()
            .addOnSuccessListener { result ->
                internshipList.clear()
                for (document in result) {
                    val internship = document.toObject(Internship::class.java)
                    internshipList.add(internship)
                }
                internshipAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to load internships: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

