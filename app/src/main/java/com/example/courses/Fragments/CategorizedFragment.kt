package com.example.courses.Fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.courses.Activities.EnrollmentActivity
import com.example.courses.Adapters.CategorizedAdapter
import com.example.courses.Data.AllCourses
import com.example.courses.R
import com.google.firebase.firestore.FirebaseFirestore

class CategorizedFragment : Fragment() {
    private lateinit var adapter: CategorizedAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_categorized, container, false)
        val heading = view.findViewById<TextView>(R.id.headName)

        // Receiving the arguments (Bundle)
        val title = arguments?.getString("categoryName")
        if (title != null) {
            heading.text = title
            Log.d("CategorizedFragment", "Category: $title")
        } else {
            Log.e("CategorizedFragment", "Category name is null!")
        }

        recyclerView = view.findViewById(R.id.categorizedRV)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        db = FirebaseFirestore.getInstance()

        // Initialize adapter with a click listener
        adapter = CategorizedAdapter(requireContext(), mutableListOf()) { courseName ->
            // When a course item is clicked, start NotesAndVideoActivity with course name
            val intent = Intent(requireContext(), EnrollmentActivity::class.java)
            intent.putExtra("course_name", courseName)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Fetch courses that match the passed category name
        if (title != null) {
            fetchCoursesByCategory(title)
        }

        // Back button setup
        val back = view.findViewById<ImageButton>(R.id.backToCategory)
        back.setOnClickListener {
            findNavController().popBackStack()  // Use this for simple back navigation
        }

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchCoursesByCategory(category: String) {
        db.collection("AllCourses")
            .whereEqualTo("category", category) // Filtering by category directly in FireStore query
            .get()
            .addOnSuccessListener { documents ->
                val courses = mutableListOf<AllCourses>()
                for (document in documents) {
                    val course = AllCourses(
                        document["name"].toString(),
                        document["brief"].toString(),
                        document["price"].toString(),
                        document["image"].toString(),
                        document["time"].toString(),
                        document["category"].toString()
                    )
                    courses.add(course)
                }
                adapter.allCourses.clear()
                adapter.allCourses.addAll(courses)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("CategorizedFragment", "Error fetching documents: ", exception)
            }
    }
}


