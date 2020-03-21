package com.example.android_project_bike

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_timer.*
import org.w3c.dom.Text
import android.os.CountDownTimer
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.widget.FrameLayout
import com.google.firebase.auth.FirebaseAuth


class BikeDetailsActivity : BaseActivity(), OnMapReadyCallback {

    lateinit var mMap: GoogleMap

    private var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    lateinit var bike: Bike
    lateinit var bikeId: String
    lateinit var dialog: Dialog
    lateinit var countDownTimer: CountDownTimer
    var timeLeftInMilliSec = MAX_RESERVATION_TIME


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bike_details)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        Log.d("USER", "${currentUser!!.email}")

        bike = Bike()

        val intent = intent
        val bundle = intent.getBundleExtra("bundle")
        bikeId = bundle.getString("bikeId")!!

        val bikeTitle = findViewById<TextView>(R.id.bike_label)
        val bikeText = "Bike $bikeId"
        bikeTitle.text = bikeText

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val rentBikeButton = findViewById<Button>(R.id.rent_bike_button)
        val reserveBikeButton = findViewById<Button>(R.id.reserve_bike_button)

        rentBikeButton.setOnClickListener {

            val intent = Intent(this, EnterQrCode::class.java)
            intent.putExtra("bundle", bundle)
            intent.putExtra("latitude", bike.position.latitude)
            intent.putExtra("longitude", bike.position.longitude)
            startActivity(intent)
        }

        dialog = Dialog(this)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.custom_popup)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val fragment = dialog.findViewById<FrameLayout>(R.id.timer_fragment)
        var timer = fragment.findViewById<TextView>(R.id.timer)


        //TODO: User can rent and reserve only one bike

        reserveBikeButton.setOnClickListener {

            dialog.show()
            startStopTimer(timer)

            db.collection("bikes").document("$bikeId")
                .update(
                    mapOf(
                        "available" to false,
                        "current_user" to currentUser.email
                    )
                )
                .addOnSuccessListener { result ->
                    Log.d("SUCCESS", "Added $result")
                }
                .addOnFailureListener { exception ->
                    Log.d("ERROR", "Adding data failed!")
                }


            //TODO: Scan Code --> Proceed to ScanActivity

            val cancelReservationButton = dialog.findViewById<Button>(R.id.cancel_reservation_button)

            cancelReservationButton.setOnClickListener {
                cancelReservation(dialog, timer)
            }

            val scanQRCodeButton = dialog.findViewById<Button>(R.id.scan_code_button)

            scanQRCodeButton.setOnClickListener {
                val intent = Intent(this, EnterQrCode::class.java)
                intent.putExtra("bundle", bundle)
                startActivity(intent)
            }


        }
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
        const val MAX_RESERVATION_TIME = 1800000.toLong()
    }

    fun startStopTimer(timer: TextView) {

        countDownTimer = object : CountDownTimer(timeLeftInMilliSec, 1000) {
            override fun onTick(l: Long) {
                timeLeftInMilliSec = l
                timer.text = updateTimer()


            }

            override fun onFinish() {
                cancelReservation(dialog, timer)
            }
        }.start()

    }



    fun updateTimer(): String {
        var minutes = timeLeftInMilliSec / 60000 as Int
        var seconds = timeLeftInMilliSec % 60000 / 1000 as Int
        var timeLeft: String
        var min = minutes.toString()
        var sec = seconds.toString()

        if (seconds < 10) {
            sec = "0" + sec
        }

        if (minutes < 10) {
            min = "0" + min
        }

        timeLeft = "$min :" + " $sec"
        return timeLeft

    }

    fun cancelReservation(dialog: Dialog, timer: TextView) {

        dialog.dismiss()
        countDownTimer.cancel()
        timeLeftInMilliSec = MAX_RESERVATION_TIME


        db.collection("bikes").document("$bikeId")
            .update(
                mapOf(
                    "available" to true,
                    "current_user" to ""
                )
            )
            .addOnSuccessListener { result ->
                Log.d("SUCCESS", "Added $result")
            }
            .addOnFailureListener { exception ->
                Log.d("ERROR", "Adding data failed!")
            }
    }

}
