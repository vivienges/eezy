package com.example.android_project_bike

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class TourDetailService : Service() {

    private lateinit var bikeId: String
    private lateinit var bundle: Bundle
    private lateinit var rideRefString: String
    private lateinit var terminateReceiver: BroadcastReceiver
    private lateinit var bike: Bike
    private lateinit var ride: Ride
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    private lateinit var builder: Notification.Builder
    private val channelId = "TourDetailNotification"
    private val description = "eezy Tour Details"
    private var db = FirebaseFirestore.getInstance()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        bundle = intent?.getBundleExtra(BUNDLE)!!
        bikeId = bundle.getString(TourDetailsActivity.BIKE_ID)!!
        rideRefString = bundle.getString(RIDE_DEF_STRING)!!
        listenToDatabaseChanges()
        super.onCreate()
        terminateReceiver = object : BroadcastReceiver() {
            override fun onReceive(arg0: Context, intent: Intent) {
                val action = intent.action
                if (action == TERMINATE_DATA_SERVICE) {
                    Log.d("TERMINATE", "Terminated data receiver service!")
                    notificationManager.cancel(1)
                    unregisterReceiver(terminateReceiver)
                    stopSelf()
                }
            }
        }
        registerReceiver(terminateReceiver, IntentFilter(TERMINATE_DATA_SERVICE))
        return START_STICKY
    }

    private fun listenToDatabaseChanges() {
        db.collection(BIKES).document(bikeId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("FAIL", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    Log.d("DATA", "Current data: ${snapshot.data}")
                    bike = snapshot.toObject(Bike::class.java)!!
                    val bikeChangedIntent = Intent(NEW_BIKE_DATA)
                    bikeChangedIntent.putExtra(CHARGE, bike.charge)
                    bikeChangedIntent.putExtra(LATITUDE, bike.position.latitude)
                    bikeChangedIntent.putExtra(LONGITUDE, bike.position.longitude)
                    bikeChangedIntent.putExtra(LOCKED, bike.locked)
                    sendBroadcast(bikeChangedIntent)
                    updateNotification()
                } else {
                    Log.d("NULL", "Current data: null")
                }
            }
        db.collection(RIDES).document(rideRefString)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("FAIL", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    ride = snapshot.toObject(Ride::class.java)!!
                    val rideChangedIntent = Intent(NEW_RIDE_DATA)
                    rideChangedIntent.putExtra(PRICE, ride.total_price)
                    rideChangedIntent.putExtra(DISTANCE, ride.total_km)
                    sendBroadcast(rideChangedIntent)
                    updateNotification()
                }
            }
    }

    private fun updateNotification(){
        if (::ride.isInitialized && ::bike.isInitialized) {
            val contentView = RemoteViews(packageName, R.layout.notification_layout)
            contentView.setTextViewText(R.id.notification_charge_label, "${bike.charge} %")
            contentView.setTextViewText(
                R.id.notification_distance_label,
                "${String.format("%.2f", ride.total_km)} km"
            )
            contentView.setTextViewText(
                R.id.notification_price_label,
                "${String.format("%.1f", ride.total_price)} SEK"
            )

            val resultIntent = Intent(this, TourDetailsActivity::class.java)
            resultIntent.putExtra(BUNDLE, bundle)
            resultIntent.putExtra(REFRESH, "REFRESH")
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val resultPendingIntent: PendingIntent? =
                TaskStackBuilder.create(applicationContext).run {
                    addNextIntentWithParentStack(resultIntent)
                    getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
                }
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationChannel = NotificationChannel(
                    channelId,
                    description,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(notificationChannel)
                builder = Notification.Builder(this, channelId)
                    .setContent(contentView)
                    .setContentIntent(resultPendingIntent)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.eezy_icon)
                    .setLargeIcon(
                        BitmapFactory.decodeResource(
                            this.resources,
                            R.drawable.eezy_icon
                        )
                    )
            } else {
                builder = Notification.Builder(this)
                    .setContent(contentView)
                    .setContentIntent(resultPendingIntent)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.eezy_icon)
                    .setLargeIcon(
                        BitmapFactory.decodeResource(
                            this.resources,
                            R.drawable.eezy_icon
                        )
                    )
            }
            startForeground(1, builder.build())
        }
    }

    companion object {
        const val BIKES = "bikes"
        const val RIDES = "rides"
        const val BUNDLE = "bundle"
        const val RIDE_DEF_STRING = "rideRefString"
        const val NEW_BIKE_DATA = "BIKE_DATA_CHANGED"
        const val NEW_RIDE_DATA = "RIDE_DATA_CHANGED"
        const val TERMINATE_DATA_SERVICE = "TERMINATE_DATA_SERVICE"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val LOCKED = "locked"
        const val PRICE = "price"
        const val DISTANCE = "distance"
        const val CHARGE = "charge"
        const val REFRESH = "refresh"
    }
}
