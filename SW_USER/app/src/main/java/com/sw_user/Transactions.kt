package com.sw_user

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class Transactions : AppCompatActivity() {

    private lateinit var tableContent: TableLayout

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseFirestore

    private lateinit var uID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)

        tableContent = findViewById(R.id.tableContent)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseFirestore.getInstance()

        uID = firebaseAuth.currentUser!!.uid

        getOrderDetailsFromDatabase()
    }

    private fun getOrderDetailsFromDatabase() {
        val ordersRef: CollectionReference =
            firebaseDatabase.collection("client").document(uID).collection("orders")
        ordersRef.get().addOnSuccessListener { orders ->
            for(order in orders) {
                val plateNumber = order.get("plate number")
                val dateReserved = order.get("date reserved")
                val price = order.get("price")
                val orderCompleted = order.get("order completed")

                val tableRow = TableRow(this)
                tableRow.layoutParams = TableRow.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
                )

                val plateNumberView = TextView(this).apply {
                    text = plateNumber.toString()
                    layoutParams = TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
                    gravity = Gravity.CENTER
                }
                val dateReservedView = TextView(this).apply {
                    text = dateReserved.toString()
                    layoutParams = TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
                    gravity = Gravity.CENTER
                }
                val priceView = TextView(this).apply {
                    text = price.toString()
                    layoutParams = TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
                    gravity = Gravity.CENTER
                }
                val orderCompletedView = TextView(this).apply {
                    text = orderCompleted.toString()
                    layoutParams = TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
                    gravity = Gravity.CENTER
                }

                tableRow.addView(plateNumberView)
                tableRow.addView(dateReservedView)
                tableRow.addView(priceView)
                tableRow.addView(orderCompletedView)

                tableContent.addView(tableRow)
            }
        }
    }

    fun goToDashboard(view: View) {
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
    }
}