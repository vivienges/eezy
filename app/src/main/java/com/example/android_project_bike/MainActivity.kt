package com.example.android_project_bike

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
import androidx.core.content.withStyledAttributes
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import java.util.*
import java.util.jar.Manifest
import android.location.Location as Location

class MainActivity : BaseActivity(), OnMapReadyCallback {

    private var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private lateinit var mMap: GoogleMap
    lateinit var adapter: ArrayAdapter<String>
    private var idList = mutableListOf<String>()
    private lateinit var listView: ListView
    private lateinit var availability: TextView
    lateinit var bike: Bike
    private var loggedIn = false
    private lateinit var bikeString: String

    private var locationGPS : Location? = null
    private var locationNetwork : Location? = null
    private lateinit var locationManager : LocationManager
    private var hasGPS = false
    private var hasNetwork = false
    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private val REQUEST_CODE = 101
    private lateinit var userMarker : Marker


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        auth = FirebaseAuth.getInstance()
        bike = Bike()
        bikeString =  resources.getString(R.string.bike)

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
            bundle.putString(BIKE_ID, itemText)

            if (loggedIn != true) {

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

   private fun fetchLocation() {

       if (ActivityCompat.checkSelfPermission(
               this,
               "ACCESS_FINE_LOCATION"
           ) != PackageManager.PERMISSION_GRANTED
       ) {
           ActivityCompat.requestPermissions(
               this,
               arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
               REQUEST_CODE
           )

       } else {
           Log.d("ERROR", "Permission already granted")
       }


           locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
           hasGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
           hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

           if (hasGPS || hasNetwork) {

               if (hasGPS) {
                   locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, object:LocationListener {
                       override fun onLocationChanged(location: Location?) {
                           if (location != null) {
                               locationGPS = location

                               var userLocation = LatLng(locationGPS!!.latitude, locationGPS!!.longitude)

                               if (!::userMarker.isInitialized) {
                                   userMarker = mMap.addMarker(MarkerOptions()
                                       .position(userLocation)
                                       .title("You are here")
                                       .icon(BitmapDescriptorFactory.defaultMarker(20F))
                                   )

                               }
                               else {
                                   userMarker.position = userLocation

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
                           Toast.makeText(this@MainActivity, "The Provider was enabled", Toast.LENGTH_LONG).show()
                       }

                       override fun onProviderDisabled(provider: String?) {
                           Toast.makeText(this@MainActivity, "The Provider was disabled", Toast.LENGTH_LONG).show()
                           userMarker.remove()

                       }

                   })

                   val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                   if (localGpsLocation != null) {
                       locationGPS = localGpsLocation
                   }
               }

               if(hasNetwork) {
                   locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0F, object:LocationListener {
                       override fun onLocationChanged(location: Location?) {
                           if (location != null) {
                               locationNetwork = location}
                       }

                       override fun onStatusChanged(
                           provider: String?,
                           status: Int,
                           extras: Bundle?
                       ) {
                           TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                       }

                       override fun onProviderEnabled(provider: String?) {
                           Toast.makeText(this@MainActivity, "The Provider was enabled", Toast.LENGTH_LONG).show()
                       }

                       override fun onProviderDisabled(provider: String?) {
                           Toast.makeText(this@MainActivity, "The Provider was disabled", Toast.LENGTH_LONG).show()
                           userMarker.remove()
                       }


                   })

                   val localNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                   if (localNetworkLocation != null) {
                       locationNetwork = localNetworkLocation
                   }
               }

               else if (locationGPS != null && locationNetwork != null) {
                   if(locationGPS!!.accuracy > locationNetwork!!.accuracy) {

                       Log.d("Network", "Network latitude: ${locationNetwork!!.latitude}")
                       Log.d("Network", "Network longitude: ${locationNetwork!!.longitude}")
                   }

                   else {
                       Log.d("GPS", "Network latitude: ${locationGPS!!.latitude}")
                       Log.d("GPS", "Network longitude: ${locationGPS!!.longitude}")
                   }
               }

           }
           else {

               val alertTitle = getString(R.string.location_off)
               val alertMessage = getString(R.string.use_location)
               val alertYes = getString(R.string.yes)
               val alertNo = getString(R.string.no)

               AlertDialog.Builder(this)
                   .setTitle(alertTitle)
                   .setMessage(alertMessage)
                   .setPositiveButton(alertYes) {
                           dialog, whichButton ->
                       dialog.dismiss()

                       val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                       startActivity(intent)

                   }
                   .setNegativeButton(alertNo) {
                           dialog, whichButton ->
                       dialog.cancel()
                       dialog.dismiss()
                   }.show()
           }

       }



    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        loggedIn = currentUser != null

        adapter.notifyDataSetChanged()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fetchLocation()
    }


    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        val jonkoping = LatLng(57.778767, 14.163388)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jonkoping, 12F))

        var markers = mutableListOf<Marker>()

        db.collection(BIKES)
            .addSnapshotListener { snapshots, e ->
                if (e == null && snapshots != null)
                //TODO: Add error handling
                    for (documentChange in snapshots!!.documentChanges) {
                        when (documentChange.type) {
                            DocumentChange.Type.ADDED -> {

                                bike = documentChange.document.toObject(Bike::class.java)

                                val position = LatLng(bike.position.latitude, bike.position.longitude)


                                var marker = mMap.addMarker(MarkerOptions()
                                    .position(position).title("$bikeString ${documentChange.document.id}")
                                    .icon(BitmapDescriptorFactory.defaultMarker(82F)))

                                markers.add(marker)

                                if (bike.available) {

                                    idList.add("$bikeString ${documentChange.document.id}")

                                } else {
                                    markers.first { it.title == "$bikeString ${documentChange.document.id}" }
                                        .isVisible = false
                                }
                            }
                            DocumentChange.Type.MODIFIED -> {

                                bike = documentChange.document.toObject(Bike::class.java)

                                if (bike.available) {
                                    if (("$bikeString ${documentChange.document.id}") !in idList)
                                        idList.add("$bikeString ${documentChange.document.id}")

                                    markers.first { it.title == "$bikeString ${documentChange.document.id}" }
                                        .isVisible = true

                                } else {
                                    idList.remove("$bikeString ${documentChange.document.id}")
                                    markers.first { it.title == "$bikeString ${documentChange.document.id}" }
                                        .isVisible = false
                                }
                            }
                            DocumentChange.Type.REMOVED ->
                                idList.remove("$bikeString + ${documentChange.document.id}")
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
        if(requestCode == REQUEST_CODE) {

                if ( grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("GPS", "We have permission")
                }
            }
        }


    companion object {
        const val BIKE_ID = "BIKE_ID"
        const val BIKES = "bikes"
        const val BUNDLE = "bundle"
    }
}


