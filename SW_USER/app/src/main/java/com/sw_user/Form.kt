package com.sw_user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.regex.Pattern

class Form : AppCompatActivity() {

    private lateinit var inputPlateNumber: EditText
    private lateinit var plateNumberHelpButton: Button
    private lateinit var selectedDate: TextView
    private lateinit var datePicker: Button
    private lateinit var initOrderButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        inputPlateNumber = findViewById(R.id.inputPlateLicense)
        plateNumberHelpButton = findViewById(R.id.plateNumberHelpButton)
        selectedDate = findViewById(R.id.textSelectedDate)
        datePicker = findViewById(R.id.datePickerButton)
        initOrderButton = findViewById(R.id.initOrderButton)

        initOrderButton.setOnClickListener {
            val plateNumber = inputPlateNumber.text.toString()

            if (TextUtils.isEmpty(plateNumber)) {
                inputPlateNumber.error = "Plate number is required!"
                return@setOnClickListener
            }

            if(!validatePlateNumber(plateNumber)) {
                inputPlateNumber.error = "Invalid plate number!"
                return@setOnClickListener
            }
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