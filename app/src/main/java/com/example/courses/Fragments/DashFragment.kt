package com.example.courses.Fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.courses.Activities.EnrollmentActivity
import com.example.courses.Adapters.AllCoursesAdapter
import com.example.courses.Adapters.DashInternshipAdapter
import com.example.courses.Adapters.InternshipAdapter
import com.example.courses.Data.AllCourses
import com.example.courses.Data.Internship
import com.example.courses.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashFragment : Fragment() {

    private lateinit var userTextView: TextView
    private lateinit var popularRV: RecyclerView
    private lateinit var internshipRV: RecyclerView
    private lateinit var courseAdapter: AllCoursesAdapter
    private lateinit var internShipAdapter: DashInternshipAdapter
    private lateinit var imageSlider1: ImageSlider
    private lateinit var imageSlider2: ImageSlider
    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dash, container, false)

        userTextView = view.findViewById(R.id.textView2)
        popularRV = view.findViewById(R.id.popularRV)
        internshipRV = view.findViewById(R.id.internshipRV)

        // Fetch and display username
        fetchUsernameFromFirestore()

        // Set LayoutManager for RecyclerView
        popularRV.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        internshipRV.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        // Fetch and display popular courses
        fetchPopularCourses()

        // Fetch and display internShip Programs
        fetchInternships()

        // Set click listeners for cards
        val card1 = view.findViewById<CardView>(R.id.card1)
        val card2 = view.findViewById<CardView>(R.id.card2)
        val card3 = view.findViewById<CardView>(R.id.card3)
        val card4 = view.findViewById<CardView>(R.id.card4)

        // Fetch TextView associated with each card
        val nameCat1 = view.findViewById<TextView>(R.id.nameCat1)
        val nameCat2 = view.findViewById<TextView>(R.id.nameCat2)
        val nameCat3 = view.findViewById<TextView>(R.id.nameCat3)

        // Navigate to CategorizedFragment with text value of each TextView
        card1.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("categoryName", nameCat1.text.toString())
            findNavController().navigate(R.id.action_dashFragment_to_categorizedFragment, bundle)
        }

        card2.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("categoryName", nameCat2.text.toString())
            findNavController().navigate(R.id.action_dashFragment_to_categorizedFragment, bundle)
        }

        card3.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("categoryName", nameCat3.text.toString())
            findNavController().navigate(R.id.action_dashFragment_to_categorizedFragment, bundle)
        }

        card4.setOnClickListener {
            val action = R.id.action_dashFragment_to_moreFragment
            findNavController().navigate(action)
        }

        // Handle "see all" click event
        val all = view.findViewById<TextView>(R.id.see_all)
        all.setOnClickListener {
            val action = R.id.action_dashFragment_to_allFragment
            findNavController().navigate(action)
        }

        val internships = view.findViewById<TextView>(R.id.see_all_programs)
        internships.setOnClickListener {
            val action = R.id.action_dashFragment_to_allInternships
            findNavController().navigate(action)
        }

        //image Slider Logic
        // Initialize the image sliders
        imageSlider1 = view.findViewById(R.id.imageSlider1)
        imageSlider2 = view.findViewById(R.id.imageSlider2)

        // Fetch and display images for both sliders
        fetchImagesForSlider("banner1", imageSlider1)
        fetchImagesForSlider("banner2", imageSlider2)

        return view
    }

    @SuppressLint("SetTextI18n")
    private fun fetchUsernameFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserUid != null) {
            db.collection("Users")
                .document(currentUserUid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val username = document.getString("username")
                        userTextView.text = "    Hi, $username"
                    } else {
                        Log.e("DashFragment", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("DashFragment", "Error fetching username", exception)
                }
        } else {
            userTextView.text = "    Hi, Guest"
            Log.e("DashFragment", "User not logged in")
        }
    }

    private fun fetchPopularCourses() {
        val coursesRef = FirebaseFirestore.getInstance().collection("AllCourses")
        coursesRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("FireStore Error", "Listen failed: ${e.message}")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val courses = snapshot.toObjects(AllCourses::class.java)
                // Filter the courses where type is "popular" and limit to 5-6 items
                val popularCourses = courses.filter { it.type == "popular" }.take(6)

                // Initialize the adapter with the filtered courses
                courseAdapter = AllCoursesAdapter(
                    requireContext(),
                    popularCourses.toMutableList(),
                    object : AllCoursesAdapter.OnItemClickListener {
                        override fun onItemClick(course: AllCourses) {
                            val intent = Intent(requireActivity(), EnrollmentActivity::class.java)
                            intent.putExtra("course_name", course.name)
                            startActivity(intent)
                        }
                    }
                )
                popularRV.adapter = courseAdapter
            } else {
                Log.d("FireStore", "No courses found")
            }
        }
    }

    private fun fetchInternships() {
        val coursesRef = FirebaseFirestore.getInstance().collection("InternShips")
        coursesRef.limit(6).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("FireStore Error", "Listen failed: ${e.message}")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val internships = snapshot.toObjects(Internship::class.java)
                // Limit to 5-6 items (already limited by Firestore query)
                val someInternships = internships.take(6)

                // Initialize the adapter with the filtered internships
                internShipAdapter = DashInternshipAdapter(
                    someInternships
                )

                // Set the click listener for the adapter
                internShipAdapter.setOnItemClickListener(object : DashInternshipAdapter.OnItemClickListener {
                    override fun onItemClick(course: Internship) {
                        val intent = Intent(requireActivity(), EnrollmentActivity::class.java)
                        intent.putExtra("INTERNSHIP_NAME", course.name) // Pass the name of the internship
                        startActivity(intent) // Start the EnrollmentActivity
                    }
                })

                // Set the adapter to the RecyclerView
                internshipRV.adapter = internShipAdapter
            } else {
                Log.d("FireStore", "No internships found")
            }
        }
    }

    private fun fetchImagesForSlider(bannerKey: String, imageSlider: ImageSlider) {
        db.collection("Banners")
            .document("uP0aZwr7wg1dMHaY7No2") // document name, containing banners
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val imageUrls = document.get(bannerKey) as? List<String>
                    if (!imageUrls.isNullOrEmpty()) {
                        val slideModels = imageUrls.map { SlideModel(it, ScaleTypes.FIT) } // Map URLs to SlideModel
                        imageSlider.setImageList(slideModels, ScaleTypes.FIT) // Load images into the slider
                    } else {
                        Log.w("MainActivity", "No images found for $bannerKey.")
                    }
                } else {
                    Log.w("MainActivity", "Document not found!")
                }
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Error fetching images for $bannerKey", e)
            }
    }

}



