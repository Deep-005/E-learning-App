package com.example.courses.EntryFragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.courses.Activities.DashActivity
import com.example.courses.Objects.SharedPreferencesManager
import com.example.courses.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class RegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences
    private val RC_SIGN_IN = 100 // Request code for Google sign-in
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signUpBtn:Button
    private lateinit var progressBar: ProgressBar

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        // Initialize Firebase Authentication and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        // Navigate back to login
        val login = view.findViewById<TextView>(R.id.loginBack)
        login.setOnClickListener {
            val action = R.id.action_registerFragment_to_loginFragment
            findNavController().navigate(action)
            activity?.overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        // Register button click listener
        signUpBtn = view.findViewById(R.id.signUpBtn)
        progressBar = view.findViewById(R.id.progressRegister)
        progressBar.visibility=View.GONE // initially not visible
        signUpBtn.setOnClickListener {
            val nameEditText = view.findViewById<EditText>(R.id.editTextUserNameRegister)
            val emailEditText = view.findViewById<EditText>(R.id.editTextEmailRegister)
            val phoneNumberEditText = view.findViewById<EditText>(R.id.editTextNumberRegister)
            val passwordEditText = view.findViewById<EditText>(R.id.editTextPasswordRegister)
            val confirmPasswordEditText = view.findViewById<EditText>(R.id.etPasswordRegister)

            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val phoneNumber = phoneNumberEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (validateInput(name, email, phoneNumber, password, confirmPassword)) {
                if (password == confirmPassword) {
                    // Change button text  and show the progress bar
                    signUpBtn.text = "Signing Up..."
                    progressBar.visibility = View.VISIBLE
                    registerUser(name, email, phoneNumber, password)
                } else {
                    // Change button text  and hide the progress bar
                    signUpBtn.text = "Sign Up"
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Replace with your client ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        // Google sign-in button click listener
        val googleSignInButton = view.findViewById<ImageView>(R.id.googleRegister)
        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

        return view
    }

    private fun registerUser(name: String, email: String, phoneNumber: String, password: String) {
        val registerBtn = view?.findViewById<Button>(R.id.signUpBtn)
        registerBtn?.isEnabled = false // Disable the button to prevent multiple clicks

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                registerBtn?.isEnabled = true // Re-enable the button

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(context, "User registered successfully!", Toast.LENGTH_SHORT).show()

                    if (user != null) {
                        saveUserInfoToFirestore(user, name, phoneNumber)
                        navigateToMainActivity()
                    }
                } else {
                    Toast.makeText(context, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                registerBtn?.isEnabled = true // Re-enable the button
                Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to save user info in Firestore
    private fun saveUserInfoToFirestore(user: FirebaseUser, name: String, phoneNumber: String) {
        val userInfo = mapOf(
            "username" to name,
            "uid" to user.uid,
            "email" to user.email,
            "phoneNumber" to phoneNumber,
            "dateCreated" to Date(),
            "certificates" to emptyList<String>() // Initialize certificates as an empty array
        )

        firestore.collection("Users").document(user.uid)
            .set(userInfo)
            .addOnSuccessListener {
                Log.d("Firestore", "User info saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error saving user info: ", e)
                Toast.makeText(context, "Failed to save user info", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateInput(name: String, email: String, mobile: String, password: String, confirmPassword: String): Boolean {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(mobile)) {
            Toast.makeText(context, "Please enter your mobile number", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(password) || password.length < 6) {
            Toast.makeText(context, "Please enter a valid password (at least 6 characters)", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(context, "Please confirm your password", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun navigateToMainActivity() {
        val intent = Intent(context, DashActivity::class.java)
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.slide_up, R.anim.no_animation)
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException) {
                Toast.makeText(context, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        // Change button text  and show the progress bar
        signUpBtn.text = "Signing Up..."
        progressBar.visibility = View.VISIBLE

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val name = user.displayName ?: "Anonymous"
                        val email = user.email ?: "N/A"
                        val phoneNumber = user.phoneNumber ?: "N/A"

                        // Save user information in the correct format
                        val userInfo = mapOf(
                            "username" to name,
                            "uid" to user.uid,
                            "email" to email,
                            "phoneNumber" to phoneNumber,
                            "dateCreated" to Date(),
                            "certificates" to emptyList<String>() // Initialize certificates as an empty array
                        )

                        // Save to Firestore
                        firestore.collection("Users").document(user.uid)
                            .set(userInfo)
                            .addOnSuccessListener {
                                Log.d("Firestore", "User info saved successfully")
                                navigateToMainActivity() // Navigate to Dashboard
                            }
                            .addOnFailureListener { e ->
                                // Change button text  and hide the progress bar
                                signUpBtn.text = "Sign Up"
                                progressBar.visibility = View.GONE
                                Log.e("Firestore", "Error saving user info: ", e)
                                Toast.makeText(context, "Failed to save user info", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Change button text  and hide the progress bar
                    signUpBtn.text = "Sign Up"
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


}
