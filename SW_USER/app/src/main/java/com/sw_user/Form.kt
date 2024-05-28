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
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern

class Form : AppCompatActivity() {

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var dbRefPlateNumbers: DatabaseReference

    private lateinit var plateNumberObject: PlateNumber
    private var maxCapacity: Int = 0
    private var priceWeekday: Int = 0
    private var priceWeekend: Int = 0

    private lateinit var inputPlateNumber: EditText
    private lateinit var plateNumberHelpButton: Button
    private lateinit var selectedDate: TextView
    private lateinit var datePicker: Button
    private lateinit var textPrice: TextView
    private lateinit var initOrderButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        firebaseDatabase = FirebaseDatabase.getInstance()

        plateNumberObject = PlateNumber()
        maxCapacity = 0
        priceWeekday = 2
        priceWeekend = 3

        inputPlateNumber = findViewById(R.id.inputPlateLicense)
        plateNumberHelpButton = findViewById(R.id.plateNumberHelpButton)
        selectedDate = findViewById(R.id.textSelectedDate)
        datePicker = findViewById(R.id.datePickerButton)
        textPrice = findViewById(R.id.textPrice)
        initOrderButton = findViewById(R.id.initOrderButton)

        getMaxCapacity()

        datePicker.setOnClickListener {
            createDatePicker()
        }

        initOrderButton.setOnClickListener { view ->

            val plateNumberText = inputPlateNumber.text.toString()

            if (TextUtils.isEmpty(plateNumberText)) {
                inputPlateNumber.error = "Plate number is required!"
                return@setOnClickListener
            }

            if(!validatePlateNumber(plateNumberText)) {
                inputPlateNumber.error = "Invalid plate number!"
                return@setOnClickListener
            }

            if(selectedDate.text.isNullOrEmpty()) {
                Toast.makeText(this@Form, "Please select a date range!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            initiateOrder(view, plateNumberText)
        }
    }

    private fun validatePlateNumber(input: String): Boolean {
        val regexPattern = "^(((AB|AG|AR|BC|BH|BN|BR|BT|BV|BZ|CJ|CL|CS|CT|CV|DB|DJ|GJ|GL|GR|HD|HR|IF|IL|IS|MH|MM|MS|NT|OT|PH|SB|SJ|SM|SV|TL|TM|TR|VL|VN|VS) (0[1-9]|[1-9][0-9]))|(B (0[1-9]|[1-9][0-9]{1,2}))) ([A-HJ-NPR-Z][A-PR-Z]{2})$"
        val pattern: Pattern = Pattern.compile(regexPattern)
        val matcher = pattern.matcher(input)
        return matcher.matches()
    }

    private fun getMaxCapacity() {
        val dbRefMaxCapacity: DatabaseReference = firebaseDatabase.getReference("/maxCapacity/maxCapacity")
        dbRefMaxCapacity.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val maxCapacityString = dataSnapshot.getValue(String::class.java)
                maxCapacity = maxCapacityString?.toInt()!!
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("QUERY_ERR","Database Error: ${databaseError.message}")
            }
        })
    }

//    private fun millisecondsToDate(milliseconds: Long): String {
//        val calendar = Calendar.getInstance()
//        calendar.timeInMillis = milliseconds
//        return calendar.toString()
//    }

    private fun millisecondsToDateWithFormat(milliseconds: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliseconds
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun createDatePicker() {
        val materialDatePicker = MaterialDatePicker.Builder.datePicker()

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC+2"))
        calendar.add(Calendar.DAY_OF_YEAR, 0)
        val todayDate = calendar.timeInMillis
        val currentDateString = millisecondsToDateWithFormat(todayDate)
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
            val chosenDateInMillis: Long = selection
            val chosenDate: String = millisecondsToDateWithFormat(chosenDateInMillis)
            calendar.timeInMillis = chosenDateInMillis

            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            selectedDate.visibility = View.VISIBLE
            selectedDate.text = chosenDate

            plateNumberObject.emptyReservedDates()
            plateNumberObject.addReservedDate(chosenDate)

            val buildTextPrice = when (dayOfWeek) {
                Calendar.SATURDAY -> "Price: $priceWeekend€"
                Calendar.SUNDAY -> "Price: $priceWeekend€"
                else -> "Price: $priceWeekday€"
            }
            textPrice.text = buildTextPrice
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun initiateOrder (view: View, plateNumberText: String) {

        val initOrderDialog = AlertDialog.Builder(view.context)
        initOrderDialog.setTitle("Confirm Order?")
        initOrderDialog.setMessage("Please double check your order details one more time!")

        initOrderDialog.setPositiveButton("Confirm!") { dialog, which ->

            dbRefPlateNumbers = firebaseDatabase.getReference("plateNumbers").push()

            plateNumberObject.plateNumber = plateNumberText
            plateNumberObject.isPermanent = false

            dbRefPlateNumbers.setValue(plateNumberObject)
                .addOnSuccessListener {
                    val orderCompletedDialog = AlertDialog.Builder(view.context)
                    orderCompletedDialog.setTitle("Thank you, order completed!")
                    orderCompletedDialog.setMessage("Your order details can be found in 'Transaction History' section!")
                    orderCompletedDialog.setPositiveButton("Ok") { dialog, which ->
                        goToDashboard(view)
                    }
                    orderCompletedDialog.show()
                }
        }

        initOrderDialog.setNegativeButton("Go Back!") { dialog, which -> /* close dialog */ }

        initOrderDialog.show()
    }

    fun goToPlateNumberPage(view: View) {
        val url = "https://en.wikipedia.org/wiki/Vehicle_registration_plates_of_Romania"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(url))
        startActivity(intent)
    }

    fun goToDashboard(view: View) {
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
    }
}