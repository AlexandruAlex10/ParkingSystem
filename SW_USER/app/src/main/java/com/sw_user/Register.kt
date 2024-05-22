package com.sw_user

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity() {

    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var registerButton: Button
    private lateinit var textLoginAccount: TextView

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        firebaseAuth = FirebaseAuth.getInstance()

        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        registerButton = findViewById(R.id.registerButton)
        textLoginAccount = findViewById(R.id.textLoginAccount)

        if(firebaseAuth.currentUser != null) {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }

        registerButton.setOnClickListener {

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

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@Register, "User Created!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                    } else {
                        Toast.makeText(this@Register, "Error: " + task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}
