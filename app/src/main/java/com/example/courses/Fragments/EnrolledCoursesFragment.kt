package com.example.courses.Fragments

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.courses.Adapters.EnrolledCoursesAdapter
import com.example.courses.Adapters.InternshipEnrolledAdapter
import com.example.courses.Data.AllCourses
import com.example.courses.Data.Internship
import com.example.courses.R
import com.google.firebase.firestore.FirebaseFirestore

class EnrolledCoursesFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerView: RecyclerView
    private lateinit var internshipRecyclerView: RecyclerView
    private lateinit var adapter: EnrolledCoursesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_enrolled_courses, container, false)

        // Setup RecyclerView for courses
        recyclerView = view.findViewById(R.id.userCoursesRV)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Setup RecyclerView for internships
        internshipRecyclerView = view.findViewById(R.id.internshipsEnrolledRV)
        internshipRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        
        // Fetch the courses from FireStore
        coursesEnrolled()

        // Fetch the internships from the fireStore
        internshipsEnrolled()

        return view
    }

    private fun coursesEnrolled() {
        sharedPreferences = requireContext().getSharedPreferences("course_prefs", MODE_PRIVATE)
        val courseNames = sharedPreferences.getStringSet("course_names", emptySet())

        val db = FirebaseFirestore.getInstance()
        val allCoursesRef = db.collection("AllCourses")

        allCoursesRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val allCourses = task.result.toObjects(AllCourses::class.java)
                val adapterCourses = mutableListOf<AllCourses>()

                for (course in allCourses) {
                    if (courseNames?.contains(course.name) == true) {
                        adapterCourses.add(course)
                    }
                }

                if (adapterCourses.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    view?.findViewById<TextView>(R.id.userCoursesRVEmptyText)?.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    view?.findViewById<TextView>(R.id.userCoursesRVEmptyText)?.visibility = View.GONE
                    if (courseNames != null) {
                        adapter = EnrolledCoursesAdapter(adapterCourses, courseNames.toList())
                    }
                    recyclerView.adapter = adapter
                }
            } else {
                Log.d("Error", "Error getting courses: ${task.exception}")
            }
        }
    }

    private fun internshipsEnrolled() {
        val internshipPreferences = requireContext().getSharedPreferences("internship_prefs", MODE_PRIVATE)
        val enrolledInternshipNames = internshipPreferences.getStringSet("internship_names", emptySet())?.toList() ?: listOf()

        val db = FirebaseFirestore.getInstance()
        val internshipsRef = db.collection("InternShips")

        internshipsRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val allInternships = task.result.toObjects(Internship::class.java)
                val enrolledInternships = allInternships.filter { enrolledInternshipNames.contains(it.name) }

                if (enrolledInternships.isEmpty()) {
                    internshipRecyclerView.visibility = View.GONE
                    view?.findViewById<TextView>(R.id.userInternshipsEnrolledRVEmptyText)?.visibility = View.VISIBLE
                } else {
                    internshipRecyclerView.visibility = View.VISIBLE
                    view?.findViewById<TextView>(R.id.userInternshipsEnrolledRVEmptyText)?.visibility = View.GONE
                    val internshipAdapter = InternshipEnrolledAdapter(enrolledInternships, enrolledInternshipNames)
                    internshipRecyclerView.adapter = internshipAdapter
                }
            } else {
                Log.d("Error", "Error getting internships: ${task.exception}")
            }
        }
    }


}