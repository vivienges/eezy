package com.example.android_project_bike

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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import android.os.CountDownTimer
import android.widget.FrameLayout
import android.widget.Toast
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.auth.FirebaseAuth


class BikeDetailsActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    private lateinit var bike: Bike
    private lateinit var bikeId: String
    private lateinit var dialog: Dialog
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var finishReceiver: BroadcastReceiver
    private var timeLeftInMilliSec = MAX_RESERVATION_TIME
    private var bikeReserved = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bike_details)

        finishReceiver = object : BroadcastReceiver() {
            override fun onReceive(arg0: Context, intent: Intent) {
                val action = intent.action
                if (action == FINISH_BIKE_ACTIVITY) {
                    finish()
                }
            }
        }
        registerReceiver(finishReceiver, IntentFilter(FINISH_BIKE_ACTIVITY))

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        bike = Bike()

        val intent = intent
        val bundle = intent.getBundleExtra(BUNDLE)
        bikeId = bundle?.getString(BIKE_ID)!!

        val bikeTitle = findViewById<TextView>(R.id.bike_label)
        val bikeText = "${resources.getString(R.string.bike)} $bikeId"
        bikeTitle.text = bikeText

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val rentBikeButton = findViewById<Button>(R.id.rent_bike_button)
        val reserveBikeButton = findViewById<Button>(R.id.reserve_bike_button)

        rentBikeButton.setOnClickListener {
            db.collection(USERS).document(currentUser!!.uid)
                .get()
                .addOnSuccessListener { result ->
                    if (result.data != null) {
                        Log.d("DATA", "Current data: ${result.data}")
                        val payment = result[PAYMENT]

                        if (payment == 0.toLong()) {
                            Log.d("PAYMENT", "$payment")

                            val intent = Intent(this, AddPaymentActivity::class.java)
                            intent.putExtra(BUNDLE, bundle)
                            intent.putExtra(LATITUDE, bike.position.latitude)
                            intent.putExtra(LONGITUDE, bike.position.longitude)
                            startActivity(intent)
                        }
                        else {
                            val intent = Intent(this, EnterQrCodeActivity::class.java)
                            intent.putExtra(BUNDLE, bundle)
                            intent.putExtra(LATITUDE, bike.position.latitude)
                            intent.putExtra(LONGITUDE, bike.position.longitude)
                            startActivity(intent)
                        }
                    }
                }
        }

        dialog = Dialog(this)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.reservation_popup)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val fragment = dialog.findViewById<FrameLayout>(R.id.timer_fragment)
        val timer = fragment.findViewById<TextView>(R.id.timer)

        reserveBikeButton.setOnClickListener {
            dialog.show()
            startStopTimer(timer)
            bikeReserved = true
            db.collection(BIKES).document(bikeId)
                .update(
                    mapOf(
                        RESERVED to true,
                        AVAILABLE to false,
                        CURRENT_USER to currentUser?.email
                    )
                )
                .addOnSuccessListener { result ->
                    Log.d("SUCCESS", "Added $result")
                }
                .addOnFailureListener { exception ->
                    Log.d("ERROR", "Adding data failed!")
                }

            bikeReserved = true

            val cancelReservationButton = dialog.findViewById<Button>(R.id.cancel_reservation_button)

            cancelReservationButton.setOnClickListener {
                db.collection(BIKES).document(bikeId)
                    .update(
                        mapOf(
                            RESERVED to false,
                            CURRENT_USER to "",
                            AVAILABLE to true
                        )
                    )
                    .addOnSuccessListener { result ->
                        Log.d("SUCCESS", "Modified $result")
                    }
                    .addOnFailureListener{
                        Log.d("ERROR", "Modifying bike data failed!")
                    }
                bikeReserved = false
                dialog.dismiss()
            }
            // Code can only be entered manually
            val scanQRCodeButton = dialog.findViewById<Button>(R.id.scan_code_button)
            scanQRCodeButton.setOnClickListener {
                val intent = Intent(this, EnterQrCodeActivity::class.java)
                intent.putExtra(BUNDLE, bundle)
                startActivity(intent)
            }
        }
    }

    override fun onBackPressed() {
        if (!bikeReserved) {
            super.onBackPressed()
        }
        else {
            Toast.makeText(
                this,
                getString(R.string.stay_on_page),
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(finishReceiver)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        db.collection(BIKES).document(bikeId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("FAIL", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    bike = snapshot.toObject(Bike::class.java)!!
                    val info = findViewById<TextView>(R.id.charge_val_label)
                    info.text = "${bike.charge}"

                    val position =
                        LatLng(bike.position.latitude, bike.position.longitude)
                    mMap.addMarker(MarkerOptions()
                        .position(position).title("${resources.getString(R.string.bike)} $bikeId")
                        .icon(BitmapDescriptorFactory.defaultMarker(82F)))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18F))

                } else {
                    Log.d("NULL", "Current data: null")
                }
            }
    }

    fun startStopTimer(timer: TextView) {
        countDownTimer = object : CountDownTimer(timeLeftInMilliSec, 1000) {
            override fun onTick(l: Long) {
                timeLeftInMilliSec = l
                timer.text = updateTimer()
            }

            override fun onFinish() {
                cancelReservation(dialog)
            }
        }.start()
    }

    fun updateTimer(): String {
        val minutes = timeLeftInMilliSec / 60000
        val seconds = timeLeftInMilliSec % 60000 / 1000
        val timeLeft: String
        var min = minutes.toString()
        var sec = seconds.toString()

        if (seconds < 10) {
            sec = "0$sec"
        }
        if (minutes < 10) {
            min = "0$min"
        }
        timeLeft = "$min : $sec"
        return timeLeft
    }

    fun cancelReservation(dialog: Dialog) {
        dialog.dismiss()
        countDownTimer.cancel()
        timeLeftInMilliSec = MAX_RESERVATION_TIME
        bikeReserved = false

        db.collection(BIKES).document(bikeId)
            .update(
                mapOf(
                    AVAILABLE to true,
                    CURRENT_USER to ""
                )
            )
            .addOnSuccessListener { result ->
                Log.d("SUCCESS", "Added $result")
            }
            .addOnFailureListener { exception ->
                Log.d("ERROR", "Adding data failed!")
            }
    }

    companion object {
        const val BIKE_ID = "BIKE_ID"
        const val BIKES = "bikes"
        const val USERS = "users"
        const val AVAILABLE = "available"
        const val RESERVED = "reserved"
        const val CURRENT_USER = "current_user"
        const val FINISH_BIKE_ACTIVITY = "finish_bike_details_activity"
        const val MAX_RESERVATION_TIME = 1800000.toLong()
        const val BUNDLE = "bundle"
        const val LONGITUDE = "longitude"
        const val LATITUDE = "latitude"
        const val PAYMENT = "payment"
    }

}
