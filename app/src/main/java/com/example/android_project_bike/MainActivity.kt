package com.example.android_project_bike

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.*
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.model.CameraPosition
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.google.android.gms.maps.model.Marker
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange


class MainActivity : BaseActivity(), OnMapReadyCallback {


    private var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private lateinit var mMap: GoogleMap
    lateinit var adapter : ArrayAdapter<String>
    private var idList= mutableListOf<String>()
    private lateinit var listView: ListView
    private lateinit var availability : TextView
    lateinit var bike: Bike
    private var loggedIn = false


    // Set up multidex for this activity
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        auth = FirebaseAuth.getInstance()

        bike = Bike()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        idList = mutableListOf()
        listView = findViewById(R.id.list_view)
        availability = findViewById(R.id.bikes_availability_label)

        adapter = ArrayAdapter(
            applicationContext,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            idList
        )

        listView.adapter = adapter

        var bundle = Bundle()

        listView.setOnItemClickListener { parent, view, position, id ->

            val itemText = listView.getItemAtPosition(position).toString().replace("[^0-9]".toRegex(), "")
            bundle.putString("bikeId", itemText)

            if (loggedIn != true) {

                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.putExtra("bundle", bundle)
                startActivity(intent)

            } else {

                val intent = Intent(this@MainActivity, BikeDetailsActivity::class.java)
                intent.putExtra("bundle", bundle)
                startActivity(intent)

            }

        }
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        loggedIn = currentUser != null

        adapter.notifyDataSetChanged()
    }


    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        val jonkoping = LatLng(57.778767, 14.163388)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jonkoping, 12F))

        var markers = mutableListOf<Marker>()

        db.collection("bikes")
            .addSnapshotListener { snapshots, e ->
                if (e == null && snapshots != null)
                //TODO: Add error handling
                    for (documentChange in snapshots!!.documentChanges) {
                        when (documentChange.type) {
                            DocumentChange.Type.ADDED -> {

                                bike = documentChange.document.toObject(Bike::class.java)

                                val position = LatLng(bike.position.latitude, bike.position.longitude)
                                var marker = mMap.addMarker(MarkerOptions().position(position).title("Bike ${documentChange.document.id}"))
                                markers.add(marker)

                                if (bike.available) {

                                    idList.add("Bike " + documentChange.document.id)

                                }

                                else {
                                    markers.first { it.title == "Bike ${documentChange.document.id}"}.isVisible = false
                                }
                            }
                            DocumentChange.Type.MODIFIED -> {

                                bike = documentChange.document.toObject(Bike::class.java)

                                if (bike.available) {
                                    if ( ("Bike " + documentChange.document.id) !in idList)
                                        idList.add("Bike " + documentChange.document.id)

                                    markers.first { it.title == "Bike ${documentChange.document.id}"}.isVisible = true

                                }

                                else {
                                    idList.remove("Bike " + documentChange.document.id)
                                    markers.first { it.title == "Bike ${documentChange.document.id}"}.isVisible = false
                                }
                            }
                            DocumentChange.Type.REMOVED ->
                                idList.remove("Bike " + documentChange.document.id)

                        }
                    }
                idList.sort()
                adapter.notifyDataSetChanged()

                if (idList.isEmpty()) {
                    availability.visibility = View.VISIBLE
                    listView.visibility = View.GONE
                }

                else {
                    availability.visibility = View.GONE
                    listView.visibility = View.VISIBLE
                }
            }

    }

    companion object {
        const val EXTRA_BIKE_ID = "BIKE_ID"
    }
}


