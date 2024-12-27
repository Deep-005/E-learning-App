package com.example.courses.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.courses.Activities.CertificatesActivity
import com.example.courses.Activities.EditActivity
import com.example.courses.Activities.EntryActivity
import com.example.courses.Data.Feedback
import com.example.courses.R
import com.example.courses.databinding.ActivityFeedbackBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.UUID

class UserFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userTextView: TextView
    private lateinit var profilePicUser: CircleImageView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance()

        // get username and email
        userTextView = view.findViewById(R.id.uname)
        fetchUsernameFromFirestore()

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val userEmail = firebaseUser ?.email
        val eMail = view.findViewById<TextView>(R.id.email)
        eMail.text = userEmail


        // editProfile activity traversing
        val editProfileLayout = view?.findViewById<RelativeLayout>(R.id.editProfile)
        editProfileLayout?.setOnClickListener {
           editProfile()
        }

        // certificates activity traversing
        val certificateLayout = view?.findViewById<RelativeLayout>(R.id.certificates)
        certificateLayout?.setOnClickListener {
           certificates()
        }

        // my courses activity traversing
        val coursesLayout = view?.findViewById<RelativeLayout>(R.id.courses)
        coursesLayout?.setOnClickListener {
            val action = R.id.action_userFragment_to_paidFragment
            findNavController().navigate(action)
        }

        // logout action layout call
        val logoutLayout = view.findViewById<RelativeLayout>(R.id.logout)
        logoutLayout.setOnClickListener {
            logout()
        }

        // feedback form/activity
        val feedbackLayout = view.findViewById<RelativeLayout>(R.id.feedback)
        feedbackLayout.setOnClickListener {
            context?.let { it1 -> showFeedbackForm(it1) }
        }

        // support button action
        val supportLayout = view.findViewById<RelativeLayout>(R.id.support)
        supportLayout?.setOnClickListener {
            openGmailApp()
        }

        // about Us layout (got to Our webpage)
        val aboutUsLayout = view.findViewById<RelativeLayout>(R.id.aboutUs)
        aboutUsLayout?.setOnClickListener {
            openWebPage("https://febtech.in/")
        }

        // add image icon button
        val addImg = view.findViewById<CircleImageView>(R.id.addImg)
        addImg?.setOnClickListener {
            openGallery()
        }

        profilePicUser = view.findViewById(R.id.profilePic)

        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val selectedImageUri: Uri? = result.data?.data
                if (selectedImageUri != null) {
                    uploadImageToFirebase(selectedImageUri)
                } else {
                    Toast.makeText(requireContext(), "Image not selected", Toast.LENGTH_SHORT).show()
                }
            }
        }

        loadProfilePicFromPreferences() // Load and display saved profile picture


        return view
    }

    // Function to open the gallery
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        // Show a Toast to inform the user
        Toast.makeText(requireContext(), "Please wait for few seconds while your image is being uploaded and saved...", Toast.LENGTH_LONG).show()

        val storageReference = FirebaseStorage.getInstance().reference
        val profilePicRef = storageReference.child("profile_pics/${UUID.randomUUID()}.jpg")

        val uploadTask = profilePicRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            profilePicRef.downloadUrl.addOnSuccessListener { downloadUri ->
                saveImageUrlToPreferences(downloadUri.toString())
                fetchAndDisplayImage(downloadUri.toString())
                Toast.makeText(requireContext(), "Image uploaded and saved successfully!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Image upload failed. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageUrlToPreferences(url: String) {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("profilePicUrl", url).apply()
    }

    private fun fetchAndDisplayImage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.user_bg) // Default image
            .into(profilePicUser)
    }

    private fun loadProfilePicFromPreferences() {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedUrl = sharedPreferences.getString("profilePicUrl", null)
        if (savedUrl != null) {
            fetchAndDisplayImage(savedUrl)
        } else {
            profilePicUser.setImageResource(R.drawable.user_bg) // Default image
        }
    }

    // Function to open the webpage in default browser
    private fun openWebPage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        startActivity(intent)
    }

    private fun openGmailApp() {
        val recipient = "example@example.com" // default mail where the user response will be sent to

        // Create the email intent
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$recipient") // Attach the recipient in the URI
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient)) // This is technically optional now
            putExtra(Intent.EXTRA_SUBJECT, "Subject here")
            putExtra(Intent.EXTRA_TEXT, "Body of the email")
        }
        startActivity(intent)
    }

    private fun showFeedbackForm(context: Context) {
        val dialog = Dialog(context)
        val binding = ActivityFeedbackBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        // Set up Firebase database reference
        val database = FirebaseDatabase.getInstance().getReference("Feedbacks")

        // Submit button listener
        binding.submit.setOnClickListener {
            val rating = binding.ratingBar.rating
            val feedback = binding.feedback.text.toString()

            val feedbackId = database.push().key
            val feedbackData = Feedback(rating, feedback)

            feedbackId?.let {
                database.child(it).setValue(feedbackData).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(context, "Failed to submit feedback.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Later button listener
        binding.later.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun logout(){
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.logout_dialog_box)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val yesButton = dialog.findViewById<Button>(R.id.yes)
        yesButton.setOnClickListener {
            firebaseAuth.signOut()
            activity.let {
                val intent = Intent(it, EntryActivity::class.java)
                startActivity(intent)
                activity?.overridePendingTransition(R.anim.slide_up, R.anim.no_animation)
            }
        }

        val noButton = dialog.findViewById<Button>(R.id.no)
        noButton.setOnClickListener {
            dialog.dismiss()
        }

        val delete = dialog.findViewById<TextView>(R.id.deleteAccount)
        delete.setOnClickListener {
            deleteAccount()
            dialog.dismiss()
        }

        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun deleteAccount() {
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.delete_account_box)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val yesButton = dialog.findViewById<Button>(R.id.yes)
        val progressBar = dialog.findViewById<ProgressBar>(R.id.progressDeletion)

        yesButton.setOnClickListener {
            val user = firebaseAuth.currentUser
            if (user != null) {
                val userEmail = user.email // Get the current user's email

                // Ensure email is not null or empty
                if (userEmail.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "User email not found.", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                // Show progress bar and update button text
                progressBar.visibility = View.VISIBLE
                yesButton.text = "Deleting..."
                yesButton.isEnabled = false // Disable the button to prevent multiple clicks

                val db = FirebaseFirestore.getInstance()
                db.collection("Users")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            // Delete all matching documents
                            for (document in querySnapshot) {
                                db.collection("Users").document(document.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        Log.d("DeleteAccount", "User document deleted successfully.")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("DeleteAccount", "Failed to delete user document: ${e.message}")
                                    }
                            }
                        } else {
                            Log.d("DeleteAccount", "No matching user document found.")
                            Toast.makeText(requireContext(), "No user data found to delete.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("DeleteAccount", "Error finding user document: ${e.message}")
                        Toast.makeText(requireContext(), "Error finding user data: ${e.message}", Toast.LENGTH_LONG).show()
                    }

                // Delete the Firebase user account
                user.delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Account successfully deleted
                            firebaseAuth.signOut()
                            yesButton.text = "Account Deleted"
                            progressBar.visibility = View.GONE

                            Handler(Looper.getMainLooper()).postDelayed({
                                activity?.let {
                                    val intent = Intent(it, EntryActivity::class.java)
                                    startActivity(intent)
                                    activity?.overridePendingTransition(R.anim.slide_up, R.anim.no_animation)
                                    it.finish()
                                }
                            }, 1000) // Delay for user feedback before transitioning
                        } else {
                            // Handle errors (e.g., re-authentication required)
                            yesButton.text = "Delete"
                            progressBar.visibility = View.GONE
                            yesButton.isEnabled = true
                            Toast.makeText(
                                requireContext(),
                                "Failed to delete account: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "No user is logged in.", Toast.LENGTH_LONG).show()
            }
            dialog.dismiss() // Close the dialog
        }

        val noButton = dialog.findViewById<ImageButton>(R.id.no)
        noButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun certificates(){
        activity.let {
            val intent = Intent(it, CertificatesActivity::class.java)
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.slide_up, R.anim.no_animation)
        }

    }

    private fun editProfile(){
        activity.let {
            val intent = Intent(it, EditActivity::class.java)
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.slide_up, R.anim.no_animation)
        }

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
                        userTextView.text = "$username"
                    } else {
                        Log.e("DashFragment", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("DashFragment", "Error fetching username", exception)
                }
        } else {
            userTextView.text = "Guest"
            Log.e("DashFragment", "User not logged in")
        }
    }

}