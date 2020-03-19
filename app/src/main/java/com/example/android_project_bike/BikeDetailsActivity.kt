package com.example.android_project_bike

import android.graphics.BitmapRegionDecoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlin.concurrent.thread

class BikeDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    private lateinit var bike: Bike


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bike_details)
    }

    override fun onStart() {
        super.onStart()

        auth = FirebaseAuth.getInstance()

        bike = Bike()

        val intent = intent
        val bundle = intent.getBundleExtra("bundle")
        val bikeId = bundle?.getString("bikeId")

        db.collection("bikes").document("$bikeId")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("FAIL", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d("DATA", "Current data: ${snapshot.data}")
                    bike = snapshot.toObject(Bike::class.java)!!

                    val info = findViewById<TextView>(R.id.charge_value)
                    info.text = "${bike.charge}"

                    val position = LatLng(bike.position.latitude, bike.position.longitude)
                    mMap.addMarker(MarkerOptions().position(position).title("Bike $bikeId"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18F))

                } else {
                    Log.d("NULL", "Current data: null")
                }
            }

        val bikeTitle = findViewById<TextView>(R.id.bike_label)
        val bikeText = "Bike $bikeId"
        bikeTitle.text = bikeText

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val rentBikeButton = findViewById<Button>(R.id.rent_bike_button)

        rentBikeButton.setOnClickListener {
            val rideData = hashMapOf(
                "start_time" to FieldValue.serverTimestamp(),
                "total_price" to 0,
                "total_km" to 0,
                "route" to listOf(GeoPoint(bike.position.latitude, bike.position.longitude))
            )
            val userRef = db.collection("users").document(auth.currentUser!!.uid)
            val bikeRef = db.collection("bikes").document(bikeId!!)
            db.runBatch {
                val ride = db.collection("rides").document()
                ride.set(rideData)
                userRef.update("history", FieldValue.arrayUnion(ride.id))
                bikeRef.update(
                    mapOf(
                        "available" to false,
                        "locked" to false,
                        "current_user" to auth.currentUser!!.email
                    )
                )
            }.addOnSuccessListener {
                Log.d("SUCCESS", "")
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

    }

    companion object {
        const val EXTRA_BIKE_ID = "BIKE_ID"
    }

}
