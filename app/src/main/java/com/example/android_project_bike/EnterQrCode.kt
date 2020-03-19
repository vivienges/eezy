package com.example.android_project_bike

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EnterQrCode : AppCompatActivity() {

    private var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_qr_code)

        val bundle = intent.getBundleExtra("bundle")
        val bikeId = bundle.getString("bikeId")

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        val qrCode = findViewById<EditText>(R.id.qr_code_edit)
        val enterButton = findViewById<Button>(R.id.qr_code_button)

        enterButton.setOnClickListener {

            if (qrCode.text.toString() == bikeId) {

                db.collection("bikes").document("$bikeId")
                    .update(
                        mapOf(
                            "available" to false,
                            "current_user" to currentUser!!.email
                        )
                    )
                    .addOnSuccessListener { result ->
                        Log.d("SUCCESS", "Added $result")
                    }
                    .addOnFailureListener { exception ->
                        Log.d("ERROR", "Adding data failed!")
                    }


                val intent = Intent(this, TourDetailsActivity::class.java)
                intent.putExtra("bundle", bundle)
                startActivity(intent)
                finish()
                Toast.makeText(this, "You booked the bike", Toast.LENGTH_LONG).show()
            }

            else {
                Toast.makeText(this, "You entered the wrong code", Toast.LENGTH_LONG).show()
            }

        }
    }
}
