package com.example.courses.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.courses.R
import com.example.courses.databinding.ActivityEditBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.leaveEditProfile.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.no_animation, R.anim.slide_down)
        }

        // Update user details and password
        binding.updateDetails.setOnClickListener {
            val isUsernameFilled = binding.editName.text.toString().trim().isNotEmpty()
            val isPhoneNumberFilled = binding.editPhoneNumber.text.toString().trim().isNotEmpty()
            val isCurrentPasswordFilled = binding.oldPass.text.toString().trim().isNotEmpty()
            val isNewPasswordFilled = binding.newPass.text.toString().trim().isNotEmpty()
            val isConfirmPasswordFilled = binding.confirmPass.text.toString().trim().isNotEmpty()

            // Determine which function(s) to call based on the filled fields
            when {
                // All fields are filled (username, phone number, and password fields)
                isUsernameFilled && isPhoneNumberFilled && isCurrentPasswordFilled && isNewPasswordFilled && isConfirmPasswordFilled -> {
                    updateUserDetails()  // Updates username and/or phone number
                    changePassword()     // Updates the password
                }

                // Only username or phone number fields are filled
                isUsernameFilled || isPhoneNumberFilled -> {
                    updateUserDetails()
                }

                // Only password fields are filled
                isCurrentPasswordFilled && isNewPasswordFilled && isConfirmPasswordFilled -> {
                    changePassword()
                }

                // None of the fields are filled
                else -> {
                    Toast.makeText(this, "Please fill in all the required fields to update.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.no_animation, R.anim.slide_down)
    }

    /**
     * Updates the username and phoneNumber fields in Firestore.
     */
    private fun updateUserDetails() {
        val userId = auth.currentUser?.uid

        // Get input values
        val newUsername = binding.editName.text.toString().trim()
        val newPhoneNumber = binding.editPhoneNumber.text.toString().trim()

        // Check if user is logged in
        if (userId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare a map for the updates
        val updates = mutableMapOf<String, Any>()

        if (newUsername.isNotEmpty()) {
            updates["username"] = newUsername
        }

        if (newPhoneNumber.isNotEmpty()) {
            updates["phoneNumber"] = newPhoneNumber
        }

        // If no fields are filled, show a message
        if (updates.isEmpty()) {
            Toast.makeText(this, "Please fill at least one field to update.", Toast.LENGTH_SHORT).show()
            return
        }

        // Update Firestore with the provided fields
        firestore.collection("Users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    /**
     * Changes the user's password using Firebase Authentication.
     */
    private fun changePassword() {
        val currentPassword = binding.oldPass.text.toString().trim()
        val newPassword = binding.newPass.text.toString().trim()
        val confirmPassword = binding.confirmPass.text.toString().trim()

        if (currentPassword.isEmpty() && newPassword.isEmpty() && confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in the password fields to change your password.", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentPassword.isEmpty()) {
            Toast.makeText(this, "Please enter your current password.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Please enter a new password.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "New password and confirmation do not match.", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user != null && user.email != null) {
            // Re-authenticate user
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    // Update password
                    user.updatePassword(newPassword)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Password updated successfully.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Re-authentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Unable to authenticate user.", Toast.LENGTH_SHORT).show()
        }
    }

}
