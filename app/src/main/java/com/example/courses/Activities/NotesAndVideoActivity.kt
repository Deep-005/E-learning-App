package com.example.courses.Activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.courses.Adapters.NotesAdapter
import com.example.courses.Adapters.VideoAdapter
import com.example.courses.R
import com.example.courses.databinding.ActivityNotesAndVideoBinding
import com.google.firebase.firestore.FirebaseFirestore

class NotesAndVideoActivity : AppCompatActivity(), VideoAdapter.OnAverageProgressCallback {

    private lateinit var courseName: String
    private lateinit var internshipName: String
    private var avgProgress: Float = 0F
    private lateinit var binding: ActivityNotesAndVideoBinding
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var videosAdapter: VideoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesAndVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve courseName and internshipName from intent
        courseName = intent.getStringExtra("course_name") ?: ""
        internshipName = intent.getStringExtra("internshipName") ?: ""

        setupTopBar()
        setupQuizButton()

        // Set up video RecyclerView
        videosAdapter = VideoAdapter(this, mutableListOf(), getActiveName(), this)
        binding.videosRV.adapter = videosAdapter
        binding.videosRV.layoutManager = LinearLayoutManager(this)

        // Fetch videos based on course or internship
        fetchVideos()

        // Set up notes RecyclerView
        notesAdapter = NotesAdapter(this, mutableListOf())
        binding.notesRV.adapter = notesAdapter
        binding.notesRV.layoutManager = LinearLayoutManager(this)

        // Fetch notes based on course or internship
        fetchNotes()

        setupEnrollButtonVisibility()
        setupCertificateButton()
        setupDropdownAnimations()

        // fetch coverImage
        fetchCoverImage()
    }

    private fun setupTopBar() {
        // Determine active name and update UI accordingly
        val activeName = getActiveName()
        if (activeName.isNotEmpty()) {
            binding.thisCourse.text = activeName
            binding.courseNameTop.text = activeName
        } else {
            Toast.makeText(this, "Something went wrong. Please try again later.", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Back button functionality
        binding.backBtn.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.no_animation, R.anim.slide_down)
        }
    }

    private fun getActiveName(): String {
        return if (courseName.isNotEmpty()) courseName else internshipName
    }

    private fun setupQuizButton() {
        binding.gotoQuiz.setCompoundDrawablesWithIntrinsicBounds(R.drawable.question_mark, 0, R.drawable.lock, 0)
        checkProgressForLockIcon()

        binding.gotoQuiz.setOnClickListener {
            if (avgProgress >= 80F) {
                val intent = Intent(this, QuizActivity::class.java)
                if (courseName.isNotEmpty()) {
                    intent.putExtra("courseName", courseName)
                } else {
                    intent.putExtra("internshipName", internshipName)
                }
                startActivity(intent)
                finish()
            } else {
                quizNotice()
            }
        }
    }

    private fun setupEnrollButtonVisibility() {
        binding.scrollViewCourse.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = binding.scrollViewCourse.scrollY
            val enrollButtonHeight = binding.secondTop.height
            binding.thisCourse.visibility = if (scrollY > enrollButtonHeight) View.VISIBLE else View.GONE
        }
    }

    private fun setupCertificateButton() {
        binding.claimCertificate.setOnClickListener {
            certificateNotice()
        }
    }

    private fun setupDropdownAnimations() {
        val videoRecycle = binding.videosRV
        val dropIcon = binding.dropDown
        binding.cV.setOnClickListener {
            toggleVisibility(videoRecycle, dropIcon)
        }

        val notesRecycle = binding.notesRV
        val dropIconNotes = binding.dropDownNotes
        binding.cN.setOnClickListener {
            toggleVisibility(notesRecycle, dropIconNotes)
        }
    }

    private fun toggleVisibility(recyclerView: View, dropIcon: View) {
        if (recyclerView.visibility == View.GONE) {
            recyclerView.visibility = View.VISIBLE
            recyclerView.animate().translationY(0f).setDuration(500)
            dropIcon.rotation = 90f
        } else {
            recyclerView.animate().translationY(recyclerView.height.toFloat()).setDuration(100)
            recyclerView.visibility = View.GONE
            dropIcon.rotation = -90f
        }
    }

    private fun fetchVideos() {
        val activeName = getActiveName()
        if (courseName.isNotEmpty()) {
            videosAdapter.fetchVideos(courseName)
        } else if (internshipName.isNotEmpty()) {
            videosAdapter.fetchVideosInternship(internshipName)
        } else {
            Toast.makeText(this, "Error fetching videos.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchNotes() {
        val activeName = getActiveName()
        if (courseName.isNotEmpty()) {
            notesAdapter.fetchNotes(courseName)
        } else if (internshipName.isNotEmpty()) {
            notesAdapter.fetchNotesInternship(internshipName)
        } else {
            Toast.makeText(this, "Error fetching notes.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        fetchVideos()
        // Example: notesAdapter.fetchNotes(getActiveName())
    }

    override fun onAverageProgress(averageProgress: Float) {
        Log.d("NotesAndVideoActivity", "Average progress: $averageProgress")
        avgProgress = averageProgress
        checkProgressForLockIcon()
    }

    private fun checkProgressForLockIcon() {
        val leftDrawable = binding.gotoQuiz.compoundDrawables[0]
        val topDrawable = binding.gotoQuiz.compoundDrawables[1]
        val bottomDrawable = binding.gotoQuiz.compoundDrawables[3]

        if (avgProgress >= 80F) {
            binding.gotoQuiz.setCompoundDrawables(leftDrawable, topDrawable, null, bottomDrawable)
        } else {
            binding.gotoQuiz.setCompoundDrawables(leftDrawable, topDrawable, binding.gotoQuiz.compoundDrawables[2], bottomDrawable)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun quizNotice() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.quiz_dialog_box)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val course = dialog.findViewById<TextView>(R.id.cName_for_quiz_dialog)
        course.text = "1. ${getActiveName()}"

        val cutBtn = dialog.findViewById<ImageView>(R.id.cutQuiz)
        cutBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun certificateNotice() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.certificate_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val course = dialog.findViewById<TextView>(R.id.cName_for_certificate_dialog)
        course.text = "1. ${getActiveName()}"

        val cutBtn = dialog.findViewById<ImageView>(R.id.cutCertificate)
        cutBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun fetchCoverImage() {
        val db = FirebaseFirestore.getInstance()
        val collectionName = if (courseName.isNotEmpty()) "AllCourses" else "InternShips"
        val activeName = getActiveName()

        if (activeName.isNotEmpty()) {
            db.collection(collectionName)
                .whereEqualTo("name", activeName)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val imageUrl = document.getString("image") // Assuming "image" is the field name
                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(imageUrl)
                                .into(binding.coverImage)
                        } else {
                            Log.w("NotesAndVideoActivity", "Image URL is empty.")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("NotesAndVideoActivity", "Error fetching cover image", e)
                }
        } else {
            Toast.makeText(this, "Something went wrong. Please try again later.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

}

