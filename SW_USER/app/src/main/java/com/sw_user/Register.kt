package com.sw_user

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class Register : AppCompatActivity() {

    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var registerButton: Button
    private lateinit var loginButton: TextView

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseFirestore.getInstance()

        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)

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

            if (password.length < 8) {
                inputPassword.error = "Password must contain at least 8 characters!"
                return@setOnClickListener
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uID = firebaseAuth.currentUser!!.uid
                        val documentReference: DocumentReference =
                            firebaseDatabase.collection("client").document(uID)
                        val client: MutableMap<String, Any> = mutableMapOf()
                        client["email"] = email
                        client["uid"] = uID
                        documentReference.set(client)
                        Toast.makeText(this@Register, "User Created!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                    } else {
                        Toast.makeText(this@Register, "Error: " + task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    fun goToLogin(view: View) {
        startActivity(Intent(applicationContext, Login::class.java))
        finish()
    }
}
