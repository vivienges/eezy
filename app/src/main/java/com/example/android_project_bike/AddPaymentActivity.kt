package com.example.android_project_bike

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class AddPaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_payment)

        val confirmSignup = findViewById<Button>(R.id.confirmSignup)

        //TODO: Enable button if all payment details are set

        confirmSignup.setOnClickListener {

            val intent = Intent(this, BikeDetailsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
