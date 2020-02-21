package com.example.android_project_bike

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loggedIn = true

        val selectBike = findViewById<Button>(R.id.selectBike)

        selectBike.setOnClickListener {

            if (loggedIn != false) {

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)

            } else {


            }

        }
    }
}
