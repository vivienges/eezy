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

        val bundle = intent.getBundleExtra(BUNDLE)
        val bikeId = bundle?.getString(BIKE_ID)
        val latitude = intent.getDoubleExtra(LATITUDE, 0.0)
        val longitude = intent.getDoubleExtra(LONGITUDE, 0.0)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        val qrCode = findViewById<EditText>(R.id.qr_code_edit)
        val enterButton = findViewById<Button>(R.id.qr_code_button)

        enterButton.setOnClickListener {

            if (qrCode.text.toString() == bikeId) {
                db.collection(BIKES).document("$bikeId")
                    .update(
                        mapOf(
                            AVAILABLE to false,
                            CURRENT_USER to currentUser!!.email
                        )
                    )
                    .addOnSuccessListener { result ->
                        Log.d("SUCCESS", "Added $result")
                        val rideData = hashMapOf(
                            START_TIME to FieldValue.serverTimestamp(),
                            TOTAL_PRICE to 0,
                            TOTAL_KM to 0,
                            ROUTE to listOf(GeoPoint(latitude, longitude))
                        )
                        val userRef = db.collection(USERS).document(currentUser!!.uid)
                        val bikeRef = db.collection(BIKES).document(bikeId)
                        db.runBatch {
                            rideRef = db.collection(RIDES).document()
                            rideRef.set(rideData)
                            userRef.update(HISTORY, FieldValue.arrayUnion(rideRef))
                            bikeRef.update(
                                mapOf(
                                    AVAILABLE to false,
                                    LOCKED to false,
                                    CURRENT_USER to currentUser!!.email
                                )
                            )
                        }.addOnSuccessListener {
                            Log.d("SUCCESS", "Ride successfully created!")
                            bundle.putString(RIDE_DEF_STRING, rideRef.id)
                            val intent = Intent(this, TourDetailsActivity::class.java)
                            intent.putExtra(BUNDLE, bundle)
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
        const val BIKE_ID = "BIKE_ID"
        const val BIKES = "bikes"
        const val USERS = "users"
        const val RIDES = "rides"
        const val HISTORY = "history"
        const val AVAILABLE = "available"
        const val CURRENT_USER = "current_user"
        const val LOCKED = "locked"
        const val BUNDLE = "bundle"
        const val LONGITUDE = "longitude"
        const val LATITUDE = "latitude"
        const val START_TIME = "start_time"
        const val TOTAL_PRICE = "total_price"
        const val TOTAL_KM = "total_km"
        const val ROUTE = "route"
        const val RIDE_DEF_STRING = "rideRefString"
    }
}
