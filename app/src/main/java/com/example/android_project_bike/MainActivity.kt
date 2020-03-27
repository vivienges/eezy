package com.example.android_project_bike

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import androidx.multidex.MultiDex
import com.google.firebase.firestore.FirebaseFirestore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import android.location.Location as Location

class MainActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var auth: FirebaseAuth
    private lateinit var mMap: GoogleMap
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var listView: ListView
    private lateinit var availability: TextView
    private lateinit var bike: Bike
    private lateinit var userMarker: Marker
    private lateinit var bikeString: String
    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var db = FirebaseFirestore.getInstance()
    private var idList = mutableListOf<String>()
    private var loggedIn = false
    private var locationGPS: Location? = null
    private var locationNetwork: Location? = null
    private var hasGPS = false
    private var hasNetwork = false
    private val markers = mutableListOf<Marker>()
    private val bikes = mutableListOf<Bike>()

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        bike = Bike()
        bikeString = resources.getString(R.string.bike)

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

        val bundle = Bundle()

        listView.setOnItemClickListener { _, _, position, _ ->

            val itemText =
                listView.getItemAtPosition(position).toString().replace("[^0-9]".toRegex(), "")
            bundle.putString(BIKE_ID, itemText)

            if (!loggedIn) {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.putExtra(BUNDLE, bundle)
                startActivity(intent)

            } else {
                val intent = Intent(this@MainActivity, BikeDetailsActivity::class.java)
                intent.putExtra(BUNDLE, bundle)
                startActivity(intent)
            }
        }
    }



    private fun checkPermission() {
        val permissionGranted = ContextCompat.checkSelfPermission(
            applicationContext, ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(ACCESS_FINE_LOCATION),
                REQUEST_CODE
            )
        } else {
            Log.d("INFO", "Permission already granted")
        }
    }

    private fun fetchLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        val permissionGranted = ContextCompat.checkSelfPermission(
            applicationContext, ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            if ((hasGPS || hasNetwork)) {
                if (hasGPS) {
                    var updateMap = true
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        5000,
                        0F,
                        object : LocationListener {
                            override fun onLocationChanged(location: Location?) {
                                if (location != null) {
                                    locationGPS = location

                                    val userLocation =
                                        LatLng(locationGPS!!.latitude, locationGPS!!.longitude)

                                    if (!::userMarker.isInitialized) {
                                        userMarker = mMap.addMarker(
                                            MarkerOptions()
                                                .position(userLocation)
                                                .title("You are here")
                                                .icon(BitmapDescriptorFactory.defaultMarker(20F))
                                        )
                                    } else {
                                        userMarker.position = userLocation
                                    }
                                    if (updateMap) {
                                        mMap.moveCamera(
                                            CameraUpdateFactory.newLatLngZoom(
                                                userMarker.position, 12F
                                            )
                                        )
                                        updateBikes()
                                        updateMap = false
                                    }
                                }
                            }

                            override fun onStatusChanged(
                                provider: String?,
                                status: Int,
                                extras: Bundle?
                            ) {
                            }

                            override fun onProviderEnabled(provider: String?) {
                                Toast.makeText(
                                    this@MainActivity,
                                    resources.getString(R.string.provider_enabled),
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            override fun onProviderDisabled(provider: String?) {
                                Toast.makeText(
                                    this@MainActivity,
                                    resources.getString(R.string.provider_disabled),
                                    Toast.LENGTH_LONG
                                ).show()
                                userMarker.remove()
                            }
                        })

                    val localGpsLocation =
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                    if (localGpsLocation != null) {
                        locationGPS = localGpsLocation
                    }
                }

                if (hasNetwork) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        5000,
                        0F,
                        object : LocationListener {
                            override fun onLocationChanged(location: Location?) {
                                if (location != null) {
                                    locationNetwork = location
                                }
                            }

                            override fun onStatusChanged(
                                provider: String?,
                                status: Int,
                                extras: Bundle?
                            ) {
                                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }

                            override fun onProviderEnabled(provider: String?) {
                                Toast.makeText(
                                    this@MainActivity,
                                    resources.getString(R.string.provider_enabled),
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            override fun onProviderDisabled(provider: String?) {
                                Toast.makeText(
                                    this@MainActivity,
                                    resources.getString(R.string.provider_disabled),
                                    Toast.LENGTH_LONG
                                ).show()
                                userMarker.remove()
                            }
                        })

                    val localNetworkLocation =
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                    if (localNetworkLocation != null) {
                        locationNetwork = localNetworkLocation
                    }
                } else if (locationGPS != null && locationNetwork != null) {
                    if (locationGPS!!.accuracy > locationNetwork!!.accuracy) {

                        Log.d("Network", "Network latitude: ${locationNetwork!!.latitude}")
                        Log.d("Network", "Network longitude: ${locationNetwork!!.longitude}")
                    } else {
                        Log.d("GPS", "Network latitude: ${locationGPS!!.latitude}")
                        Log.d("GPS", "Network longitude: ${locationGPS!!.longitude}")
                    }
                }
            } else {

                val alertTitle = getString(R.string.location_off)
                val alertMessage = getString(R.string.use_location)
                val alertYes = getString(R.string.yes)
                val alertNo = getString(R.string.no)

                AlertDialog.Builder(this)
                    .setTitle(alertTitle)
                    .setMessage(alertMessage)
                    .setPositiveButton(alertYes) { dialog, whichButton ->
                        dialog.dismiss()
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(intent)
                    }
                    .setNegativeButton(alertNo) { dialog, whichButton ->
                        dialog.cancel()
                        dialog.dismiss()
                    }.show()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        loggedIn = currentUser != null

        adapter.notifyDataSetChanged()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        checkPermission()
        fetchLocation()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val jonkoping = LatLng(57.778767, 14.163388)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jonkoping, 12F))

        mMap.setOnCameraMoveListener {
            updateBikes()
        }

        db.collection(BIKES)
            .addSnapshotListener { snapshots, e ->
                if (e == null && snapshots != null)
                    for (documentChange in snapshots.documentChanges) {
                        when (documentChange.type) {
                            DocumentChange.Type.ADDED -> {
                                bike = documentChange.document.toObject(Bike::class.java)
                                bike.id = documentChange.document.id.toInt()
                                bikes.add(bike)

                                val position =
                                    LatLng(bike.position.latitude, bike.position.longitude)

                                val marker = mMap.addMarker(
                                    MarkerOptions()
                                        .position(position)
                                        .title("$bikeString ${bike.id}")
                                        .icon(BitmapDescriptorFactory.defaultMarker(82F))
                                )
                                markers.add(marker)

                                if (mMap.projection.visibleRegion.latLngBounds.contains(marker.position) && bike.available) {
                                    idList.add("$bikeString ${bike.id}")
                                }
                                else {
                                    markers.first { it.title == "$bikeString ${bike.id}" }
                                        .isVisible = false
                                }
                            }
                            DocumentChange.Type.MODIFIED -> {
                                bike = documentChange.document.toObject(Bike::class.java)
                                bike.id = documentChange.document.id.toInt()
                                bikes.remove(bikes.first {it.id == bike.id})
                                bikes.add(bike)

                                val bikeMarker =
                                    markers.first { it.title == "$bikeString ${bike.id}" }

                                if (bike.available && mMap.projection.visibleRegion.latLngBounds.contains(
                                        bikeMarker.position
                                    )
                                ) {
                                    if (("$bikeString ${bike.id}") !in idList)
                                        idList.add("$bikeString ${bike.id}")

                                    bikeMarker.isVisible = true

                                } else {
                                    idList.remove("$bikeString ${bike.id}")
                                    bikeMarker.isVisible = false
                                }
                                bikeMarker.position =
                                    LatLng(bike.position.latitude, bike.position.longitude)
                            }
                            DocumentChange.Type.REMOVED ->
                                idList.remove("$bikeString + ${bike.id}")
                        }
                    }
                idList.sort()
                adapter.notifyDataSetChanged()

                if (idList.isEmpty()) {
                    availability.visibility = View.VISIBLE
                    listView.visibility = View.GONE
                } else {
                    availability.visibility = View.GONE
                    listView.visibility = View.VISIBLE
                }
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("GPS", "We have permission")
                fetchLocation()
            }
        }
    }

    private fun updateBikes() {
        for (bike in bikes) {
            if (mMap.projection.visibleRegion.latLngBounds.contains(
                    LatLng(
                        bike.position.latitude,
                        bike.position.longitude
                    )
                ) && bike.available
            ) {
                if("$bikeString ${bike.id}" !in idList) {
                    idList.add("$bikeString ${bike.id}")
                    markers.first { it.title == "$bikeString ${bike.id}" }.isVisible = true
                }
            }
            else {
                markers.first { it.title == "$bikeString ${bike.id}" }.isVisible = false
                if ("$bikeString ${bike.id}" in idList){
                    idList.remove("$bikeString ${bike.id}")
                }
            }
        }
        idList.sort()
        adapter.notifyDataSetChanged()

        if (idList.isEmpty()) {
            availability.visibility = View.VISIBLE
            listView.visibility = View.GONE
        } else {
            availability.visibility = View.GONE
            listView.visibility = View.VISIBLE
        }
    }

    companion object {
        const val REQUEST_CODE = 101
        const val BIKE_ID = "BIKE_ID"
        const val BIKES = "bikes"
        const val BUNDLE = "bundle"
    }
}


