package com.sw_user

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth

class Login : ComponentActivity() {

    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordButton: Button
    private lateinit var registerButton: TextView

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebaseAuth = FirebaseAuth.getInstance()

        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        loginButton = findViewById(R.id.loginButton)
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton)
        registerButton = findViewById(R.id.registerButton)

        if(firebaseAuth.currentUser != null) {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }

        loginButton.setOnClickListener {

            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (TextUtils.isEmpty(email)) {
                inputEmail.error = "Email is required!"
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                inputPassword.error = "Password is required!"
                return@setOnClickListener
            }

            if (password.length < 8) {
                inputPassword.error = "Password must contain at least 8 characters!"
                return@setOnClickListener
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@Login, "Logged In Successfully!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                    } else {
                        Toast.makeText(this@Login, "Error: " + task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }

        forgotPasswordButton.setOnClickListener { view ->

            val inputEmailPasswordReset = EditText(view.context)

            val passwordResetDialog = AlertDialog.Builder(view.context)
            passwordResetDialog.setTitle("Reset Password")
            passwordResetDialog.setMessage("Enter Your Email:")
            passwordResetDialog.setView(inputEmailPasswordReset)

            passwordResetDialog.setPositiveButton("Submit") { dialog, which ->
                val email = inputEmailPasswordReset.text.toString()

                firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener {
                    Toast.makeText(this@Login, "Email sent, check your inbox!", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(this@Login, "Email not sent! Error: " + e.message, Toast.LENGTH_SHORT).show()
                }
            }

            passwordResetDialog.setNegativeButton("Cancel") { dialog, which -> /* close dialog */ }

            passwordResetDialog.create().show()
        }

    }

    fun goToRegister(view: View) {
        startActivity(Intent(applicationContext, Register::class.java))
        finish()
    }
}