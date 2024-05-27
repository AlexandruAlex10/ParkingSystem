package com.sw_user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern

class Form : AppCompatActivity() {

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var dbRefMaxCapacity: DatabaseReference

    private lateinit var plateNumber: PlateNumber
    private var maxCapacity: Int = 0

    private lateinit var inputPlateNumber: EditText
    private lateinit var plateNumberHelpButton: Button
    private lateinit var selectedDate: TextView
    private lateinit var datePicker: Button
    private lateinit var initOrderButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        firebaseDatabase = FirebaseDatabase.getInstance();
        dbRefMaxCapacity = FirebaseDatabase.getInstance().getReference("/maxCapacity/maxCapacity")

        plateNumber = PlateNumber()
        maxCapacity = 0

        inputPlateNumber = findViewById(R.id.inputPlateLicense)
        plateNumberHelpButton = findViewById(R.id.plateNumberHelpButton)
        selectedDate = findViewById(R.id.textSelectedDate)
        datePicker = findViewById(R.id.datePickerButton)
        initOrderButton = findViewById(R.id.initOrderButton)

        dbRefMaxCapacity.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val maxCapacityString = dataSnapshot.getValue(String::class.java)
                maxCapacity = maxCapacityString?.toInt()!!
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("QUERY_ERR","Database Error: ${databaseError.message}")
            }
        })

        datePicker.setOnClickListener {
            val materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC+2"))
            calendar.add(Calendar.DAY_OF_YEAR, 0)
            val todayDate = calendar.time
            val currentDateString = dateFormat.format(todayDate)
            materialDatePicker.setTitleText("Current Date: $currentDateString")
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrowInMillis = calendar.timeInMillis
            calendar.add(Calendar.MONTH, 1)
            val lastDayNextMonthInMillis = calendar.timeInMillis

            val constraintsBuilder = CalendarConstraints.Builder()
            constraintsBuilder.setStart(tomorrowInMillis)
            constraintsBuilder.setEnd(lastDayNextMonthInMillis)
            constraintsBuilder.setValidator(object : CalendarConstraints.DateValidator {
                val todayInMillis = MaterialDatePicker.todayInUtcMilliseconds()

                override fun isValid(date: Long): Boolean {
                    return date > todayInMillis
                }

                override fun writeToParcel(dest: Parcel, flags: Int) {
                    dest.writeLong(todayInMillis)
                }

                override fun describeContents(): Int {
                    return 0
                }
            })
            materialDatePicker.setCalendarConstraints(constraintsBuilder.build())

            val datePicker = materialDatePicker.build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                val startDate = selection.first
                val startDateString = dateFormat.format(Date(startDate))
                val endDate = selection.second
                val endDateString = dateFormat.format(Date(endDate))
                val selectedDateRange = "$startDateString - $endDateString"
                selectedDate.text = selectedDateRange
            }
            datePicker.show(supportFragmentManager, "DATE_RANGE_PICKER")
        }

        initOrderButton.setOnClickListener { view ->
            val plateNumber = inputPlateNumber.text.toString()

            if (TextUtils.isEmpty(plateNumber)) {
                inputPlateNumber.error = "Plate number is required!"
                return@setOnClickListener
            }

            if(!validatePlateNumber(plateNumber)) {
                inputPlateNumber.error = "Invalid plate number!"
                return@setOnClickListener
            }

            if (selectedDate.text.isNullOrEmpty()) {
                Toast.makeText(this@Form, "Please select a date range!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val initOrderDialog = AlertDialog.Builder(view.context)
            initOrderDialog.setTitle("Confirm Order?")
            initOrderDialog.setMessage("Please double check your order details one more time!")

            initOrderDialog.setPositiveButton("Confirm!") {dialog, which ->
                // send data into the database TODO

                val orderCompletedDialog = AlertDialog.Builder(view.context)
                orderCompletedDialog.setTitle("Thank you, order completed!")
                orderCompletedDialog.setMessage("Your order details can be found in 'Transaction History' section!")
                orderCompletedDialog.setPositiveButton("Ok") {dialog, which ->
                    goToDashboard(view)
                }
                orderCompletedDialog.show()
            }

            initOrderDialog.setNegativeButton("Go Back!") { dialog, which -> /* close dialog */ }

            initOrderDialog.show()
        }
    }

    private fun validatePlateNumber(input: String): Boolean {
        val regexPattern = "^(((AB|AG|AR|BC|BH|BN|BR|BT|BV|BZ|CJ|CL|CS|CT|CV|DB|DJ|GJ|GL|GR|HD|HR|IF|IL|IS|MH|MM|MS|NT|OT|PH|SB|SJ|SM|SV|TL|TM|TR|VL|VN|VS) (0[1-9]|[1-9][0-9]))|(B (0[1-9]|[1-9][0-9]{1,2}))) ([A-HJ-NPR-Z][A-PR-Z]{2})$"
        val pattern: Pattern = Pattern.compile(regexPattern)
        val matcher = pattern.matcher(input)
        return matcher.matches()
    }

    fun goToPlateNumberPage(view: View) {
        val url = "https://en.wikipedia.org/wiki/Vehicle_registration_plates_of_Romania"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(url))
        startActivity(intent);
    }

    fun goToDashboard(view: View) {
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
    }
}