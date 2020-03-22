package com.example.android_project_bike

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class EnterQrCodeActivity : AppCompatActivity() {

    private var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private lateinit var rideRef: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_qr_code)

        val bundle = intent.getBundleExtra("bundle")
        val bikeId = bundle?.getString("bikeId")
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

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
                        val rideData = hashMapOf(
                            "start_time" to FieldValue.serverTimestamp(),
                            "total_price" to 0,
                            "total_km" to 0,
                            "route" to listOf(GeoPoint(latitude, longitude))
                        )
                        val userRef = db.collection("users").document(auth.currentUser!!.uid)
                        val bikeRef = db.collection("bikes").document(bikeId)
                        db.runBatch {
                            rideRef = db.collection("rides").document()
                            rideRef.set(rideData)
                            userRef.update("history", FieldValue.arrayUnion(rideRef))
                            bikeRef.update(
                                mapOf(
                                    "available" to false,
                                    "locked" to false,
                                    "current_user" to auth.currentUser!!.email
                                )
                            )
                        }.addOnSuccessListener {
                            Log.d("SUCCESS", "Ride successfully created!")
                            bundle.putString("rideRefString", rideRef.id)
                            val intent = Intent(this, TourDetailsActivity::class.java)
                            intent.putExtra("bundle", bundle)
                            startActivity(intent)
                            val finishIntent = Intent(FINISH_ACTIVITY_FLAG)
                            sendBroadcast(finishIntent)
                            finish()
                            Toast.makeText(this, "You booked the bike", Toast.LENGTH_LONG).show()
                        }

                    }
                    .addOnFailureListener { exception ->
                        Log.d("ERROR", "Adding data failed: ", exception)
                    }
            }

            else {
                Toast.makeText(this, "You entered the wrong code", Toast.LENGTH_LONG).show()
            }

        }
    }
    companion object {
        const val FINISH_ACTIVITY_FLAG = "finish_activity"
    }
}
