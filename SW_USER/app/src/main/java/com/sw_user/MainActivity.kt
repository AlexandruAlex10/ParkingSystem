package com.sw_user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var email: TextView
    private lateinit var verifyEmailContainer: LinearLayout
    private lateinit var verifyEmailButton: Button
    private lateinit var reserveSpotButton: Button
    private lateinit var transactionsButton: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseFirestore

    private lateinit var user: FirebaseUser
    private lateinit var uID: String
    private lateinit var documentReference: DocumentReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseFirestore.getInstance()

        email = findViewById(R.id.textEmail)
        verifyEmailContainer = findViewById(R.id.verifyEmailContainer)
        verifyEmailButton = findViewById(R.id.VerifyEmailButton)
        reserveSpotButton = findViewById(R.id.reserveSpotButton)
        transactionsButton = findViewById(R.id.transactionsButton)

        uID = firebaseAuth.currentUser!!.uid
        documentReference = firebaseDatabase.collection("client").document(uID)

        documentReference.addSnapshotListener(this@MainActivity) { documentSnapshot, e ->
            email.text = documentSnapshot!!.getString("email")
        }

        user = firebaseAuth.currentUser!!

        if(!user.isEmailVerified) {

            verifyEmailContainer.visibility = View.VISIBLE
            reserveSpotButton.visibility = View.INVISIBLE
            transactionsButton.visibility = View.INVISIBLE

            verifyEmailButton.setOnClickListener{
                user.sendEmailVerification().addOnSuccessListener {
                    Toast.makeText(this@MainActivity, "Verification email sent, check your inbox!", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(this@MainActivity, "Verification email not sent! Error: " + e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun goToForm(view: View) {
        startActivity(Intent(applicationContext, Form::class.java))
        finish()
    }

    fun goToTransactions(view: View) {
        startActivity(Intent(applicationContext, Transactions::class.java))
        finish()
    }

    fun logOut(view: View) {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this@MainActivity, "Logged Out Successfully!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(applicationContext, Login::class.java))
        finish()
    }
}