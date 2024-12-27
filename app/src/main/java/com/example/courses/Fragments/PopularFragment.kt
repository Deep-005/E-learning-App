package com.example.courses.Fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.courses.Activities.EnrollmentActivity
import com.example.courses.Adapters.CourseAdapter
import com.example.courses.Data.AllCourses
import com.example.courses.R
import com.google.firebase.firestore.FirebaseFirestore

class PopularFragment : Fragment() {
    private lateinit var courseAdapter: CourseAdapter
    private lateinit var coursesRV: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var heading: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_popular, container, false)

        // Initialize UI components
        coursesRV = view.findViewById(R.id.coursesRV)
        searchView = view.findViewById(R.id.search_bar)
        heading = view.findViewById(R.id.head)

        // Set LayoutManager for RecyclerView
        coursesRV.layoutManager = LinearLayoutManager(requireContext())

        // Fetch courses data from Firebase
        val coursesRef = FirebaseFirestore.getInstance().collection("AllCourses")
        coursesRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("FireStore Error", "Listen failed: ${e.message}")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val courses = snapshot.toObjects(AllCourses::class.java)
                // Filter the courses where type is "popular"
                val popularCourses = courses.filter { it.type == "popular" }

                courseAdapter = CourseAdapter(requireContext(), popularCourses.toMutableList(), object : CourseAdapter.OnItemClickListener {
                    override fun onItemClick(course: AllCourses) {
                        val intent = Intent(requireActivity(), EnrollmentActivity::class.java)
                        intent.putExtra("course_name", course.name)
                        startActivity(intent)
                    }
                })
                coursesRV.adapter = courseAdapter
            } else {
                Log.d("FireStore", "No courses found")
            }
        }


        searchView.isFocusable = true
        searchView.isFocusableInTouchMode = true
        searchView.requestFocus()
        searchView.isIconified = true
        // Search functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (::courseAdapter.isInitialized) {
                    courseAdapter.filter.filter(newText)
                }
                return false
            }
        })
        // Show all card views when search view is not in focus
        searchView.setOnQueryTextFocusChangeListener { _,hasFocus ->
            if (!hasFocus) {
                if (::courseAdapter.isInitialized) {
                    courseAdapter.resetFilter()
                }
                heading.visibility = View.VISIBLE
                hideKeyboard()
            }else{
                heading.visibility = View.GONE
            }
        }

        // Back button setup
        val back = view.findViewById<ImageButton>(R.id.backToDash)
        back.setOnClickListener {
            findNavController().popBackStack()  // Use this for simple back navigation
        }

        return view
    }

    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().window.decorView.windowToken, 0)
    }
}

