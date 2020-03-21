package com.example.android_project_bike

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.multidex.MultiDex
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text

class TourDetailsActivity : BaseActivity(), OnMapReadyCallback {

    lateinit var mMap: GoogleMap
    private var db = FirebaseFirestore.getInstance()

    lateinit var bike: Bike
    lateinit var bikeId: String

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_details)

        val bundle = intent.getBundleExtra("bundle")
        val bikeId = bundle.getString("bikeId")
        val bikeTitle = findViewById<TextView>(R.id.title_label)
        val titleText = resources.getString(R.string.bike) + " " + bikeId
        bikeTitle.text = titleText
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        db.collection("bikes").document("$bikeId")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("FAIL", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d("DATA", "Current data: ${snapshot.data}")
                    bike = snapshot.toObject(Bike::class.java)!!

                    val info = findViewById<TextView>(R.id.charge_val_label)
                    info.text = "${bike!!.charge}"


                    val position =
                        LatLng(bike.position!!.latitude, bike.position!!.longitude)
                    mMap.addMarker(MarkerOptions().position(position).title("Bike $bikeId"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18F))

                } else {
                    Log.d("NULL", "Current data: null")
                }
            }

    }

    companion object {
        const val EXTRA_BIKE_ID = "BIKE_ID"
    }
}
