package com.sw_user

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class Transactions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)
    }

    fun goToDashboard(view: View) {
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
    }
}