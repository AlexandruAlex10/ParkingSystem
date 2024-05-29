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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern

class Form : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseRealtimeDatabase: FirebaseDatabase
    private lateinit var firebaseFirestoreDatabase: FirebaseFirestore

    private lateinit var plateNumberObject: PlateNumber
    private lateinit var foundPlateNumberId: String
    private var maxCapacity: Int = 0
    private lateinit var occupiedDates: MutableList<String>
    private var price: Int = 0

    private lateinit var inputPlateNumber: EditText
    private lateinit var plateNumberHelpButton: Button
    private lateinit var selectedDate: TextView
    private lateinit var datePicker: Button
    private lateinit var textPrice: TextView
    private lateinit var initOrderButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseRealtimeDatabase = FirebaseDatabase.getInstance()
        firebaseFirestoreDatabase = FirebaseFirestore.getInstance()

        plateNumberObject = PlateNumber()
        foundPlateNumberId = ""
        occupiedDates = ArrayList()

        inputPlateNumber = findViewById(R.id.inputPlateLicense)
        plateNumberHelpButton = findViewById(R.id.plateNumberHelpButton)
        selectedDate = findViewById(R.id.textSelectedDate)
        datePicker = findViewById(R.id.datePickerButton)
        textPrice = findViewById(R.id.textPrice)
        initOrderButton = findViewById(R.id.initOrderButton)

        addOccupiedDates()

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

            findPlateNumberByString(plateNumberText)

            initiateOrder(view, plateNumberText)
        }
    }

    private fun validatePlateNumber(input: String): Boolean {
        val regexPattern = "^(((AB|AG|AR|BC|BH|BN|BR|BT|BV|BZ|CJ|CL|CS|CT|CV|DB|DJ|GJ|GL|GR|HD|HR|IF|IL|IS|MH|MM|MS|NT|OT|PH|SB|SJ|SM|SV|TL|TM|TR|VL|VN|VS) (0[1-9]|[1-9][0-9]))|(B (0[1-9]|[1-9][0-9]{1,2}))) ([A-HJ-NPR-Z][A-PR-Z]{2})$"
        val pattern: Pattern = Pattern.compile(regexPattern)
        val matcher = pattern.matcher(input)
        return matcher.matches()
    }

    private fun findPlateNumberByString(plateNumber: String) {
        val plateNumbersRef = firebaseRealtimeDatabase.getReference("plateNumbers")
        plateNumbersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    val plateNumberData = childSnapshot.getValue<Map<String, Any>>()
                    if (plateNumberData != null) {
                        val plateNumberValue = plateNumberData["plateNumber"] as? String
                        if (plateNumberValue == plateNumber) {
                            val isPermanent = plateNumberData["isPermanent"] as? Boolean
                            val reservedDates = plateNumberData["reservedDates"] as? List<String>
                            foundPlateNumberId = childSnapshot.key.toString()
                            plateNumberObject.plateNumber = plateNumberValue
                            plateNumberObject.isPermanent = isPermanent
                            if (plateNumberObject.isPermanent == false) {
                                plateNumberObject.reservedDates = reservedDates
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("QUERY_ERR", "Database Error: ${databaseError.message}")
            }
        })
    }

    private fun getMaxCapacity() {
        val dbRefMaxCapacity: DatabaseReference = firebaseRealtimeDatabase.getReference("/maxCapacity/maxCapacity")
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

    private fun checkDateAlreadyReserved(): Boolean {
        val checkReservedDates = plateNumberObject.reservedDates.filter { it == selectedDate.text }
        return checkReservedDates.isEmpty()
    }

    private fun millisecondsToDateWithSimpleFormat(milliseconds: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliseconds
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun millisecondsToDateWithDetailedFormat(milliseconds: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliseconds
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun createDatePicker() {
        val materialDatePicker = MaterialDatePicker.Builder.datePicker()

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC+2"))
        calendar.add(Calendar.DAY_OF_YEAR, 0)
        val todayDate = calendar.timeInMillis
        val currentDateString = millisecondsToDateWithSimpleFormat(todayDate)
        materialDatePicker.setTitleText("Current Date: $currentDateString")
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrowInMillis = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        val tomorrowNextMonthInMillis = calendar.timeInMillis

        val constraintsBuilder = CalendarConstraints.Builder()
        constraintsBuilder.setStart(tomorrowInMillis)
        constraintsBuilder.setEnd(tomorrowNextMonthInMillis)
        constraintsBuilder.setValidator(object : CalendarConstraints.DateValidator {
            val todayInMillis = MaterialDatePicker.todayInUtcMilliseconds()

            override fun isValid(date: Long): Boolean {
                var countPlateNumber: Int = 0
                occupiedDates.forEach {item ->
                    if (item == millisecondsToDateWithSimpleFormat(date)) {
                        countPlateNumber++
                    }
                }
                return date in (todayInMillis + 1)..<tomorrowNextMonthInMillis && countPlateNumber < maxCapacity
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
            val chosenDate: String = millisecondsToDateWithSimpleFormat(chosenDateInMillis)
            calendar.timeInMillis = chosenDateInMillis

            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            selectedDate.visibility = View.VISIBLE
            selectedDate.text = chosenDate

            val buildTextPrice = when (dayOfWeek) {
                Calendar.SATURDAY -> {
                    price = 3
                    "Price: ${price}€"
                }
                Calendar.SUNDAY -> {
                    price = 3
                    "Price: ${price}€"
                }
                else -> {
                    price = 2
                    "Price: ${price}€"
                }
            }
            textPrice.text = buildTextPrice
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun addOccupiedDates() {
        val plateNumbersRef = firebaseRealtimeDatabase.getReference("plateNumbers")
        plateNumbersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    val plateNumberData = childSnapshot.getValue<Map<String, Any>>()
                    if (plateNumberData != null) {
                        val isPermanent = plateNumberData["isPermanent"] as? Boolean
                        if (isPermanent == false) {
                            val reservedDates = plateNumberData["reservedDates"] as? List<String>
                            reservedDates?.forEach { item ->
                                occupiedDates.add(item)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("QUERY_ERR", "Database Error: ${databaseError.message}")
            }
        })
    }

    private fun initiateOrder (view: View, plateNumberText: String) {

        val initOrderDialog = AlertDialog.Builder(view.context)
        initOrderDialog.setTitle("Confirm Order?")
        initOrderDialog.setMessage("Please double check your order details one more time!")

        initOrderDialog.setPositiveButton("Confirm!") { dialog, which ->
            if (plateNumberObject.isPermanent == false) {
                if(checkDateAlreadyReserved()) {
                    if (!plateNumberObject.plateNumber.isNullOrEmpty()) {
                        plateNumberObject.addReservedDate(selectedDate.text.toString())

                        val updateRef = firebaseRealtimeDatabase.getReference("/plateNumbers/$foundPlateNumberId/reservedDates")
                        updateRef.setValue(plateNumberObject.reservedDates).addOnSuccessListener {
                            saveOrderDetailsInDatabase()
                            completeOrder(view)
                        }
                    }
                    else {
                        val dbRefPlateNumbers: DatabaseReference = firebaseRealtimeDatabase.getReference("plateNumbers").push()
                        plateNumberObject.plateNumber = plateNumberText
                        plateNumberObject.isPermanent = false
                        plateNumberObject.addReservedDate(selectedDate.text.toString())

                        dbRefPlateNumbers.setValue(plateNumberObject).addOnSuccessListener {
                            saveOrderDetailsInDatabase()
                            completeOrder(view)
                        }
                    }
                }
                else {
                    Toast.makeText(this@Form, "This date is already reserved!", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Toast.makeText(this@Form, "This plate number is reserved by admin!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(applicationContext, Form::class.java))
                finish()
            }
        }
        initOrderDialog.setNegativeButton("Go Back!") { dialog, which -> /* close dialog */ }

        initOrderDialog.show()
    }

    private fun saveOrderDetailsInDatabase() {
        val uID = firebaseAuth.currentUser!!.uid
        val documentReference: DocumentReference =
            firebaseFirestoreDatabase.collection("client").document(uID).collection("orders").document()
        val order: MutableMap<String, Any> = mutableMapOf()
        order["plate number"] = plateNumberObject.plateNumber
        order["date reserved"] = selectedDate.text
        order["price"] = price
        order["order completed"] = millisecondsToDateWithDetailedFormat(System.currentTimeMillis())
        documentReference.set(order)
    }

    private fun completeOrder(view: View) {
        Toast.makeText(this@Form, "Thank you, order completed!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
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