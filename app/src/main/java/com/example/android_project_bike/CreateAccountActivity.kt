package com.example.android_project_bike

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class CreateAccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        val addPayment = findViewById<Button>(R.id.addPayment)

        addPayment.setOnClickListener {

            val intent = Intent(this, AddPaymentActivity::class.java)
            startActivity(intent)
        }
    }
}
