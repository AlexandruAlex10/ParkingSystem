package com.sw_user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun logOut(view: View) {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this@MainActivity, "Logged Out Successfully!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(applicationContext, Login::class.java))
        finish()
    }
}