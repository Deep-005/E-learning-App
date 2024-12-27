package com.example.courses.EntryFragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.example.courses.Activities.EntryActivity
import com.example.courses.R
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forgot_password, container, false)

        val subOtp = view.findViewById<Button>(R.id.otp)
        subOtp.setOnClickListener {
            val email = view.findViewById<EditText>(R.id.editTextForgetPFirst).text.toString().trim()
            if (email.isNotEmpty()) {
                sendPasswordResetEmail(email) { success ->
                    if (success) {
                        confirmationBox(email)
                    }
                }
            } else {
                Toast.makeText(context, "Please enter your email.", Toast.LENGTH_SHORT).show()
            }
        }

        val backBtn = view.findViewById<ImageButton>(R.id.topFPass)
        backBtn.setOnClickListener {
            goBack()
        }

        return view
    }

    /**
     * Sends a password reset email to the provided email using Firebase Authentication
     * and executes a callback on completion.
     */
    private fun sendPasswordResetEmail(email: String, callback: (Boolean) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Password reset email sent to $email",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback(true)
                } else {
                    Toast.makeText(
                        context,
                        "Error: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback(false)
                }
            }
    }

    private fun resendPasswordResetEmail(email: String) {
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Password reset email sent to $email",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Error: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun goBack() {
        val action = R.id.action_forgetPFragment_to_loginFragment
        findNavController().navigate(action)
    }

    override fun onStart() {
        super.onStart()
        view?.findViewById<LottieAnimationView>(R.id.animation)?.playAnimation()
    }

    override fun onStop() {
        super.onStop()
        view?.findViewById<LottieAnimationView>(R.id.animation)?.cancelAnimation()
    }

    @SuppressLint("SetTextI18n")
    private fun confirmationBox(email: String){
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.confirmation_box)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val sentToEmail = dialog.findViewById<TextView>(R.id.userEmail)
        sentToEmail?.text = "\t$email"

        val yesButton = dialog.findViewById<Button>(R.id.doneBtn)
        yesButton.setOnClickListener {
            activity.let {
                val intent = Intent(it, EntryActivity::class.java)
                startActivity(intent)
                activity?.overridePendingTransition(R.anim.slide_up, R.anim.no_animation)
            }
        }

        val resend = dialog.findViewById<TextView>(R.id.resend_link)
        resend.setOnClickListener {
            resendPasswordResetEmail(email)
        }

        dialog.show()
    }

}

