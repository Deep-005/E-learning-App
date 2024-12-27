package com.example.courses.Activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.courses.databinding.ActivityQuizBinding
import com.google.firebase.firestore.FirebaseFirestore

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var courseName: String
    private lateinit var internshipName: String
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the course name or internship name from the intent
        courseName = intent.getStringExtra("course_name") ?: ""
        internshipName = intent.getStringExtra("internshipName") ?: ""

        // Fetch the quiz link for the course and load it into the WebView
        if (courseName.isNotEmpty()) {
            fetchQuizLinkAndLoadWebView()
        } else if (internshipName.isNotEmpty()) {
            fetchQuizLinkAndLoadWebViewForInternship()
        } else {
            Toast.makeText(this, "Error fetching notes.", Toast.LENGTH_SHORT).show()
        }

    }

    private fun fetchQuizLinkAndLoadWebView() {
        db.collection("AllCourses")
            .whereEqualTo("name", courseName)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val quizLink = documents.first().getString("quiz")
                    if (quizLink != null) {
                        loadQuizInWebView(quizLink)
                    } else {
                        Log.e("QuizActivity", "Quiz link is null for course: $courseName")
                    }
                } else {
                    Log.e("QuizActivity", "No documents found for course: $courseName")
                }
            }
            .addOnFailureListener { e ->
                Log.e("QuizActivity", "Error fetching quiz link", e)
            }
    }

    private fun fetchQuizLinkAndLoadWebViewForInternship() {
        db.collection("InternShips")
            .whereEqualTo("name", internshipName)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val quizLink = documents.first().getString("quiz")
                    if (quizLink != null) {
                        loadQuizInWebView(quizLink)
                    } else {
                        Log.e("QuizActivity", "Quiz link is null for course: $internshipName")
                    }
                } else {
                    Log.e("QuizActivity", "No documents found for course: $internshipName")
                }
            }
            .addOnFailureListener { e ->
                Log.e("QuizActivity", "Error fetching quiz link", e)
            }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadQuizInWebView(quizLink: String) {
        binding.onlineQuiz.apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true // Enable JavaScript if needed for the quiz page
            loadUrl(quizLink)
        }
    }
}
