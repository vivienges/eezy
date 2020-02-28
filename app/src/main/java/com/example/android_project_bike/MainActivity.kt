package com.example.android_project_bike

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var mMap: GoogleMap
    lateinit var adapter : ArrayAdapter<Bike>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val listView = findViewById<ListView>(R.id.list_view)
        adapter = ArrayAdapter(
            applicationContext,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            bikeRepository.getAllBikes()
        )

        listView.adapter = adapter
        val loggedIn = true

        listView.setOnItemClickListener { parent, view, position, id ->
            val listItemId =  listView.adapter.getItemId(position+1).toInt()

            if (loggedIn != false) {

                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)

            } else {

             /*   val intent = Intent(this@MainActivity, BikeDetailActivity::class.java)
                intent.putExtra("id", listItemId)
                startActivity(intent) */

            }

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}
