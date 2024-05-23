package com.sw_user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    private lateinit var email: TextView

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseFirestore

    private lateinit var uID: String
    private lateinit var documentReference: DocumentReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseFirestore.getInstance()

        email = findViewById(R.id.textEmail)

        uID = firebaseAuth.currentUser!!.uid
        documentReference = firebaseDatabase.collection("client").document(uID)

        documentReference.addSnapshotListener(this, EventListener<DocumentSnapshot> { documentSnapshot, e ->
            email.text = documentSnapshot!!.getString("email")
        })
    }

    fun logOut(view: View) {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this@MainActivity, "Logged Out Successfully!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(applicationContext, Login::class.java))
        finish()
    }
}