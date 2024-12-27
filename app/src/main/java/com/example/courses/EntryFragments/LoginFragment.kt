package com.example.courses.EntryFragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.courses.Activities.DashActivity
import com.example.courses.Activities.OfflineActivity
import com.example.courses.MyApp
import com.example.courses.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 100 // Request code for Google Sign-In
    private lateinit var loginBtn:Button
    private lateinit var progressBar: ProgressBar

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Ensure this matches your Google API credentials
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        // Check network state from Application class
        if ((requireActivity().application as MyApp).isConnected) {
            checkUser()
        } else {
            // Navigate to OfflineActivity if offline
            activity?.let {
                val intent = Intent(it, OfflineActivity::class.java)
                startActivity(intent)
            }
        }

        // Set up the login button click listener
        loginBtn = view.findViewById(R.id.login_btn)
        progressBar = view.findViewById(R.id.progressLogin)
        progressBar.visibility = View.GONE // initially not visible
        loginBtn.setOnClickListener {
            // Change button text and show the progress bar
            loginBtn.text = "Logging In..."
            progressBar.visibility = View.VISIBLE
            val emailEditText = view.findViewById<EditText>(R.id.editTextEmail)
            val passwordEditText = view.findViewById<EditText>(R.id.editTextPassword)

            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Check if email and password are not empty
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                // Show error message
                loginBtn.text = "Log In"
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up the Google Login button click listener
        val googleLoginButton = view.findViewById<ImageView>(R.id.googleLogin)
        googleLoginButton.setOnClickListener {
            googleLogin()
        }

        // Set up the sign up button click listener
        val signUp = view.findViewById<TextView>(R.id.sign_up)
        signUp.setOnClickListener {
            val action = R.id.action_loginFragment_to_registerFragment
            findNavController().navigate(action)
            activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.stay)
        }

        // Forget password
        val forgetPassword = view.findViewById<TextView>(R.id.forgotP)
        forgetPassword.setOnClickListener {
            val action = R.id.action_loginFragment_to_forgetPFragment
            findNavController().navigate(action)
            activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.stay)
        }

        // Set up the skip button click listener
        val skip = view.findViewById<TextView>(R.id.skip)
        skip.setOnClickListener {
            skipLayout()
        }

        return view
    }

    private fun googleLogin() {
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
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        // Change button text and show the progress bar
        loginBtn.text = "Logging In..."
        progressBar.visibility = View.VISIBLE

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val email = user.email
                        if (email != null) {
                            // Check if a document already exists for this email
                            firestore.collection("Users")
                                .whereEqualTo("email", email)
                                .get()
                                .addOnSuccessListener { documents ->
                                    if (documents.isEmpty) {
                                        // Create a new document for this user
                                        val userInfo = mapOf(
                                            "username" to (user.displayName ?: "Anonymous"),
                                            "uid" to user.uid,
                                            "email" to email,
                                            "phoneNumber" to (user.phoneNumber ?: "N/A"),
                                            "dateCreated" to Date(),
                                            "certificates" to emptyList<String>() // Initialize as an empty list
                                        )
                                        firestore.collection("Users").document(user.uid)
                                            .set(userInfo)
                                            .addOnSuccessListener {
                                                Log.d("Firestore", "User info saved successfully")
                                                navigateToMainActivity()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("Firestore", "Error saving user info: ", e)
                                                Toast.makeText(context, "Failed to save user info", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        // Document exists; proceed to dashboard
                                        navigateToMainActivity()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    loginBtn.text = "Log In"
                                    progressBar.visibility = View.GONE
                                    Log.e("Firestore", "Error checking user document: ", e)
                                    Toast.makeText(context, "Error accessing user data", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            loginBtn.text = "Log In"
                            progressBar.visibility = View.GONE
                            Log.e("Firestore", "User email is null")
                        }
                    }
                } else {
                    loginBtn.text = "Log In"
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(context, DashActivity::class.java)
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.slide_up, R.anim.no_animation)
    }

    // Function to handle user login with Firebase
    @SuppressLint("SetTextI18n")
    private fun loginUser(email: String, password: String) {
        val loginBtn = view?.findViewById<Button>(R.id.login_btn)
        loginBtn?.isEnabled = false // Disable the button to prevent multiple clicks
        val progressBar = view?.findViewById<ProgressBar>(R.id.progressLogin)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                loginBtn?.isEnabled = true // Re-enable the button

                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), DashActivity::class.java)
                    startActivity(intent)
                    activity?.overridePendingTransition(R.anim.slide_up, R.anim.no_animation)
                } else {
                    // If sign in fails, display a message to the user.
                    loginBtn?.text = "Log In"
                    progressBar?.visibility = View.GONE
                    Toast.makeText(requireContext(), "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                loginBtn?.isEnabled = true // Re-enable the button
                loginBtn?.text = "Log In"
                progressBar?.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            val intent = Intent(requireContext(), DashActivity::class.java)
            startActivity(intent)
            activity?.overridePendingTransition(0, 0)
            activity?.finish()
        } else {
            auth.signOut()
        }
    }

    private fun skipLayout() {
        activity?.let {
            val intent = Intent(it, DashActivity::class.java)
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.slide_up, R.anim.no_animation)
        }
    }
}
