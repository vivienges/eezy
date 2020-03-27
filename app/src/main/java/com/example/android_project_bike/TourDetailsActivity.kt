package com.example.android_project_bike

import android.app.ActivityManager
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.multidex.MultiDex
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.return_popup.*
import kotlin.math.round

class TourDetailsActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var bikeId: String
    private lateinit var rideRefString: String
    private lateinit var dialog: Dialog
    private lateinit var rideDataReceiver: BroadcastReceiver
    private lateinit var bikeDataReceiver: BroadcastReceiver
    private lateinit var bundle: Bundle
    private var refresh = false
    private var db = FirebaseFirestore.getInstance()

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_details)

        bundle = intent.getBundleExtra(BUNDLE)!!
        bikeId = bundle.getString(BIKE_ID)!!
        refresh = intent.getStringExtra(REFRESH) != null
        rideRefString = bundle.getString(RIDE_DEF_STRING)!!

        val bikeTitle = findViewById<TextView>(R.id.title_label)
        val titleText = resources.getString(R.string.bike) + " " + bikeId
        bikeTitle.text = titleText
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val pauseRideButton = findViewById<Button>(R.id.pause_bike_button)
        val returnBikeButton = findViewById<Button>(R.id.return_bike_button)

        val priceLabel = findViewById<TextView>(R.id.price_var_label)
        val distanceLabel = findViewById<TextView>(R.id.distance_var_label)
        val chargeLabel = findViewById<TextView>(R.id.current_charge_val_label)

        rideDataReceiver = object : BroadcastReceiver() {
            override fun onReceive(arg0: Context, intent: Intent) {
                val action = intent.action
                if (action == NEW_RIDE_DATA) {
                    val price = intent.getDoubleExtra(PRICE, 0.0)
                    val distance = intent.getDoubleExtra(DISTANCE, 0.0)
                    if( price != 0.0){
                        val currentPriceRounded = "${String.format("%.1f", price)} SEK"
                        priceLabel.text = currentPriceRounded
                    }
                    if(distance != 0.0){
                        val currentDistanceRounded = "${String.format("%.2f", distance)} km"
                        distanceLabel.text = currentDistanceRounded
                    }
                }
            }
        }
        registerReceiver(rideDataReceiver, IntentFilter(NEW_RIDE_DATA))

        bikeDataReceiver = object : BroadcastReceiver() {
            override fun onReceive(arg0: Context, intent: Intent) {
                val action = intent.action
                if (action == NEW_BIKE_DATA) {
                    val latitude = intent.getDoubleExtra(LATITUDE, 0.0)
                    val longitude = intent.getDoubleExtra(LONGITUDE, 0.0)
                    val charge = intent.getIntExtra(CHARGE, 0)
                    val locked = intent.getBooleanExtra(LOCKED, true)
                    if(charge != 0){
                        val chargeText = "$charge %"
                        chargeLabel.text = chargeText
                    }
                    if(latitude != 0.0 && longitude != 0.0){
                        val position = LatLng(latitude, longitude)
                        mMap.addMarker(
                            MarkerOptions().position(position)
                                .title("${resources.getString(R.string.bike)} $bikeId")
                        )
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18F))
                    }
                    if(!locked && pauseRideButton.text == resources.getString(R.string.continue_ride)) {
                        pauseRideButton.text = resources.getString(R.string.pause_ride)
                        Toast.makeText(
                            this@TourDetailsActivity,
                            resources.getString(R.string.ride_continued),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
        registerReceiver(bikeDataReceiver, IntentFilter(NEW_BIKE_DATA))

        returnBikeButton.setOnClickListener {
            db.runBatch {
                db.collection(BIKES).document(bikeId)
                    .update(
                        mapOf(
                            AVAILABLE to true,
                            CURRENT_USER to "",
                            LOCKED to true
                        )
                    )
                db.collection(RIDES).document(rideRefString)
                    .update(END_TIME, FieldValue.serverTimestamp())
            }
            var totalPrice: Double
            db.collection(RIDES).document(rideRefString)
                .get()
                .addOnSuccessListener { result ->
                    totalPrice = result[TOTAL_PRICE] as Double
                    val totalPriceRounded = String.format("%.1f", totalPrice)
                    val confirmationText = "$totalPriceRounded SEK"
                    dialog = Dialog(this)
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.setCancelable(false)
                    dialog.setContentView(R.layout.return_popup)
                    dialog.final_price_label.text = confirmationText
                    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.show()
                    val okButton = dialog.ok_button
                    okButton.setOnClickListener{
                        dialog.dismiss()
                        val terminateDataServiceIntent = Intent(TERMINATE_DATA_SERVICE)
                        sendBroadcast(terminateDataServiceIntent)
                        finish()
                    }
                }
        }

        pauseRideButton.setOnClickListener {
            if (pauseRideButton.text == "Pause Ride") {
                db.collection(BIKES).document(bikeId)
                    .update(LOCKED, true)
                    .addOnSuccessListener { result ->
                        Log.d("SUCCESS", "Added $result")
                    }
                    .addOnFailureListener { exception ->
                        Log.d("ERROR", "Adding data failed!")
                    }
                pauseRideButton.text = resources.getString(R.string.continue_ride)
                Toast.makeText(this, resources.getString(R.string.ride_paused), Toast.LENGTH_LONG).show()
            }

            else {
                db.collection(BIKES).document(bikeId)
                    .update(LOCKED, false)
                    .addOnSuccessListener { result ->
                        Log.d("SUCCESS", "Added $result")
                    }
                    .addOnFailureListener { exception ->
                        Log.d("ERROR", "Adding data failed!")
                    }
                pauseRideButton.text = resources.getString(R.string.pause_ride)
                Toast.makeText(this, resources.getString(R.string.ride_continued), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean{
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(rideDataReceiver)
        unregisterReceiver(bikeDataReceiver)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (!isServiceRunning(TourDetailService::class.java)) {
            val serviceIntent = Intent(this, TourDetailService::class.java)
            serviceIntent.putExtra(BUNDLE, bundle)
            startService(serviceIntent)
        }
        if (refresh) {
            val priceLabel = findViewById<TextView>(R.id.price_var_label)
            val distanceLabel = findViewById<TextView>(R.id.distance_var_label)
            val chargeLabel = findViewById<TextView>(R.id.current_charge_val_label)
            db.collection(RIDES).document(rideRefString)
                .get()
                .addOnSuccessListener { result ->
                    val priceText = String.format("%.1f", result[TOTAL_PRICE]) + " SEK"
                    priceLabel.text = priceText
                    val distanceText = String.format("%.2f", result[TOTAL_KM]) + " km"
                    distanceLabel.text = distanceText
                }
            db.collection(BIKES).document(bikeId)
                .get()
                .addOnSuccessListener { result ->
                    val chargeText = "${result[CHARGE]} %"
                    chargeLabel.text = chargeText
                    val geoPosition = result[POSITION] as GeoPoint
                    val position = LatLng(geoPosition.latitude, geoPosition.longitude)
                    mMap.addMarker(
                        MarkerOptions().position(position)
                            .title("${resources.getString(R.string.bike)} $bikeId")
                    )
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18F))
                }
            refresh = false
        }
    }

    companion object {
            const val BIKE_ID = "BIKE_ID"
            const val BIKES = "bikes"
            const val RIDES = "rides"
            const val AVAILABLE = "available"
            const val CURRENT_USER = "current_user"
            const val LOCKED = "locked"
            const val BUNDLE = "bundle"
            const val REFRESH = "refresh"
            const val END_TIME = "end_time"
            const val TOTAL_PRICE = "total_price"
            const val RIDE_DEF_STRING = "rideRefString"
            const val NEW_BIKE_DATA = "BIKE_DATA_CHANGED"
            const val NEW_RIDE_DATA = "RIDE_DATA_CHANGED"
            const val TERMINATE_DATA_SERVICE = "TERMINATE_DATA_SERVICE"
            const val LATITUDE = "latitude"
            const val LONGITUDE = "longitude"
            const val PRICE = "price"
            const val DISTANCE = "distance"
            const val CHARGE = "charge"
            const val TOTAL_KM = "total_km"
            const val POSITION = "position"
    }
}
