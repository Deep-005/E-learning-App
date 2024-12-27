package com.example.courses.Activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.courses.Data.AllCourses
import com.example.courses.Data.Internship
import com.example.courses.R
import com.example.courses.databinding.ActivityEnrollmentBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class EnrollmentActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var binding: ActivityEnrollmentBinding
    private val db = FirebaseFirestore.getInstance()
    private var courseName: String? = null
    private var internshipName: String? = null
    private var isCourseEnrollment = false
    private var isInternshipApplication = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Checkout.preload(applicationContext)
        val co = Checkout()
        val key = getString(R.string.razorPayID)
        co.setKeyID(key) // add test/live key for the payment gateway

        courseName = intent.getStringExtra("course_name")
        internshipName = intent.getStringExtra("INTERNSHIP_NAME")

        binding = ActivityEnrollmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (courseName != null) {
            setupForCourse()
        } else if (internshipName != null) {
            setupForInternship()
        } else {
            Log.e("EnrollmentActivity", "Neither course_name nor INTERNSHIP_NAME provided.")
            finish()
        }
    }

    private fun setupForCourse() {
        binding.apply {
            topic.text = courseName
            backTo.setOnClickListener { finish() }
            enroll.setOnClickListener {
                isCourseEnrollment = true
                isInternshipApplication = false
                startPayment()
            }
            topBarEnroll.setOnClickListener {
                isCourseEnrollment = true
                isInternshipApplication = false
                startPayment()
            }
            fetchCourseData(courseName)
            setupEnrollButtonVisibility()
            cards.visibility = View.VISIBLE
        }
    }

    private fun setupForInternship() {
        binding.apply {
            topic.text = internshipName
            backTo.setOnClickListener { finish() }
            enroll.setOnClickListener {
                isInternshipApplication = true
                isCourseEnrollment = false
                startPayment()
            }
            topBarEnroll.setOnClickListener {
                isInternshipApplication = true
                isCourseEnrollment = false
                startPayment()
            }
            enroll.text = getString(R.string.apply)
            topBarEnroll.text = getString(R.string.apply)
            fetchInternshipData(internshipName)
            setupEnrollButtonVisibility()
            cards.visibility = View.GONE
            textView5.visibility = View.GONE
        }
    }

    @SuppressLint("MutatingSharedPrefs")
    private fun enrollCourse(courseName: String?) {
        courseName?.let {
            val sharedPreferences = getSharedPreferences("course_prefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val courseNames = sharedPreferences.getStringSet("course_names", mutableSetOf())!!
            courseNames.add(it)
            editor.putStringSet("course_names", courseNames)
            editor.apply()
            startActivity(Intent(this, NotesAndVideoActivity::class.java).apply {
                putExtra("course_name", it)
            })
            finish()
        }
    }

    @SuppressLint("MutatingSharedPrefs")
    private fun applyInternship(internshipName: String?) {
        internshipName?.let {
            val sharedPreferences = getSharedPreferences("internship_prefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val internshipNames = sharedPreferences.getStringSet("internship_names", HashSet()) ?: HashSet()
            internshipNames.add(it)
            editor.putStringSet("internship_names", internshipNames)
            editor.apply()
            startActivity(Intent(this, NotesAndVideoActivity::class.java).apply {
                putExtra("internshipName", it)
            })
            finish()
        }
    }

    private fun fetchCourseData(courseName: String?) {
        courseName?.let {
            db.collection("AllCourses")
                .whereEqualTo("name", it)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val course = document.toObject<AllCourses>() // This line is now using KTX
                        binding.apply {
                            tagLine.text = course.tag
                            instructorName.text = course.instructor
                            price.text = course.price
                            fetchTextContent(course.about)

                            // Show the loading animation
                            demoVideoLoadingAnimation.visibility = View.VISIBLE

                            // Set up the VideoView
                            demoVideo.setVideoPath(course.demoVideo)
                            demoVideo.setOnPreparedListener { mediaPlayer ->
                                mediaPlayer.isLooping = true

                                // Hide the loading animation and start the video
                                demoVideoLoadingAnimation.visibility = View.GONE
                                demoVideo.start()
                            }

                            demoVideo.setOnErrorListener { _, _, _ ->
                                // Hide the loading animation in case of an error
                                demoVideoLoadingAnimation.visibility = View.GONE
                                Log.e("EnrollmentActivity", "Error loading video.")
                                true // Indicate that the error is handled
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("EnrollmentActivity", "Error fetching course data", e)
                }
        }
    }

    private fun fetchInternshipData(internshipName: String?) {
        internshipName?. let {
            db.collection("InternShips")
                .whereEqualTo("name", it)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val internship = document.toObject<Internship>() // This line is now using KTX
                        (binding).apply {
                            tagLine.text = internship.tag
                            instructorName.text = internship.instructor
                            price.text = internship.price
                            fetchTextContent(internship.about)

                            // Show the loading animation
                            demoVideoLoadingAnimation.visibility = View.VISIBLE

                            // Set up the VideoView
                            demoVideo.setVideoPath(internship.demoVideo)
                            demoVideo.setOnPreparedListener { mediaPlayer ->
                                mediaPlayer.isLooping = true

                                // Hide the loading animation and start the video
                                demoVideoLoadingAnimation.visibility = View.GONE
                                demoVideo.start()
                            }
                            demoVideo.setOnErrorListener { _, _, _ ->
                                // Hide the loading animation in case of an error
                                demoVideoLoadingAnimation.visibility = View.GONE
                                Log.e("EnrollmentActivity", "Error loading video.")
                                true // Indicate that the error is handled
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("EnrollmentActivity", "Error fetching internship data", e)
                }
        }
    }

    private fun fetchTextContent(link: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val content = link?.let {
                    val url = URL(it)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.inputStream.bufferedReader().use { reader -> reader.readText() }
                } ?: "No details available"
                withContext(Dispatchers.Main) {
                    (binding).aboutCourse.text = content
                }
            } catch (e: Exception) {
                Log.w("EnrollmentActivity", "Error fetching content from link", e)
            }
        }
    }

    private fun setupEnrollButtonVisibility() {
        (binding as? ActivityEnrollmentBinding)?.apply {
            scrollViewEnroll.viewTreeObserver.addOnScrollChangedListener {
                val scrollY = scrollViewEnroll.scrollY
                val enrollButtonHeight = enrollInfo.height
                topBarEnroll.visibility = if (scrollY > enrollButtonHeight) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show()
        if (isCourseEnrollment) {
            enrollCourse(courseName)
        } else if (isInternshipApplication) {
            applyInternship(internshipName)
        }
    }

    override fun onPaymentError(code: Int, description: String?) {
        paymentUnsuccessful()
        Toast.makeText(this, "Payment Declined: $description", Toast.LENGTH_SHORT).show()
    }

    private fun startPayment() {
        val activity: Activity = this
        val co = Checkout()
        try {
            val options = JSONObject()
            options.put("name", "Feb Tech IT Solutions")
            options.put("description", "An IT solutions company")
            options.put("image", "http://example.com/image/rzp.jpg")
            options.put("theme.color", "#3399cc")
            options.put("currency", "INR")

            // Fetching amount dynamically from the TextView
            val priceTextView = findViewById<TextView>(R.id.price)
            val amountText = priceTextView.text.toString() // Get the text from TextView
            val amountValue = amountText.toDoubleOrNull() ?: 0.0 // Convert to Double, default 0.0
            val amountInSubunits = (amountValue * 100).toInt() // Convert to subunits (e.g., 100.50 INR -> 10050)
            options.put("amount", amountInSubunits.toString()) // Pass amount in currency subunits

            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)

            val prefill = JSONObject()
            prefill.put("email", "")
            prefill.put("contact", "")
            options.put("prefill", prefill)

            co.open(activity, options)
        } catch (e: Exception) {
            Toast.makeText(activity, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun paymentUnsuccessful() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.payment_unsuccessful_layout)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            // Refresh the activity
            finish()
            startActivity(intent) // Restart the same activity
        }, 3000) // 3000ms = 3 seconds
    }

    // Handle lifecycle events for the VideoView
    override fun onPause() {
        super.onPause()
        binding.demoVideo.pause()
    }

    override fun onResume() {
        super.onResume()
        binding.demoVideo.start()
    }

}
