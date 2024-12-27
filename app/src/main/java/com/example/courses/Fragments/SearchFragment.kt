package com.example.courses.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.courses.Activities.EnrollmentActivity
import com.example.courses.Adapters.AllCoursesAdapter
import com.example.courses.Data.AllCourses
import com.example.courses.R
import com.google.firebase.firestore.FirebaseFirestore

class SearchFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AllCoursesAdapter
    private lateinit var searchView: SearchView
    private lateinit var headingCourses: TextView
    private var allCoursesList: MutableList<AllCourses> = mutableListOf()
    private var filteredCoursesList: MutableList<AllCourses> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.coursesAllRV)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        searchView = view.findViewById(R.id.search_bar_all)
        headingCourses = view.findViewById(R.id.headAll)

        // Initialize the adapter with the full list of courses
        adapter = AllCoursesAdapter(requireActivity(), allCoursesList, object : AllCoursesAdapter.OnItemClickListener {
            override fun onItemClick(course: AllCourses) {
                val intent = Intent(requireActivity(), EnrollmentActivity::class.java)
                intent.putExtra("course_name", course.name)
                startActivity(intent)
            }
        })
        recyclerView.adapter = adapter

        // Loading the data into the allCoursesList from the "AllCourses" collection
        val db = FirebaseFirestore.getInstance()
        db.collection("AllCourses").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    val course = document.toObject(AllCourses::class.java)
                    allCoursesList.add(course)
                }
                adapter.notifyDataSetChanged()
            } else {
                Log.d("Error", "Error getting documents: ", task.exception)
            }
        }

        // Seting up the search functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filteredCoursesList.clear()
                if (newText!!.isNotEmpty()) {
                    for (course in allCoursesList) {
                        if (course.name.lowercase().contains(newText.lowercase())) {
                            filteredCoursesList.add(course)
                        }
                    }
                    adapter = AllCoursesAdapter(requireActivity(), filteredCoursesList, object : AllCoursesAdapter.OnItemClickListener {
                        override fun onItemClick(course: AllCourses) {
                            val intent = Intent(requireActivity(), EnrollmentActivity::class.java)
                            intent.putExtra("course_name", course.name)
                            startActivity(intent)
                        }
                    })
                    recyclerView.adapter = adapter
                } else {
                    adapter = AllCoursesAdapter(requireActivity(), allCoursesList, object : AllCoursesAdapter.OnItemClickListener {
                        override fun onItemClick(course: AllCourses) {
                            val intent = Intent(requireActivity(), EnrollmentActivity::class.java)
                            intent.putExtra("course_name", course.name)
                            startActivity(intent)
                        }
                    })
                    recyclerView.adapter = adapter
                }
                return true
            }
        })

        searchView.isFocusable = true
        searchView.isFocusableInTouchMode = true
        searchView.requestFocus()
        searchView.isIconified = true

        // Reset the RecyclerView when the SearchView loses focus
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (::adapter.isInitialized) {
                    filteredCoursesList.clear()
                    filteredCoursesList.addAll(allCoursesList)
                    adapter.notifyDataSetChanged()
                }
                headingCourses.visibility = View.VISIBLE
                hideKeyboard()
            } else {
                headingCourses.visibility = View.GONE
            }
        }

        return view
    }

    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().window.decorView.windowToken, 0)
    }
}