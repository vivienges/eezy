package com.example.android_project_bike

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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text

class BikeDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var mMap: GoogleMap

    private var db = FirebaseFirestore.getInstance()

    lateinit var bike: Bike


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bike_details)
    }

    override fun onStart() {
        super.onStart()

        bike = Bike()

        val intent = intent
        val bundle = intent.getBundleExtra("bundle")
        val bikeId = bundle.getString("bikeId")

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
                    info.text = "${bike!!.charge}"

                    val position = LatLng(bike.position!!.latitude, bike.position!!.longitude)
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

        val rent_bike_button = findViewById<Button>(R.id.rent_bike_button)

        rent_bike_button.setOnClickListener() {



        }

    }




    override fun onMapReady(googleMap: GoogleMap) {

        val intent = intent
        val bikeId = intent.getStringExtra(EXTRA_BIKE_ID)

        mMap = googleMap

    }


    companion object {
        const val EXTRA_BIKE_ID = "BIKE_ID"
    }

}
