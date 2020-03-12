package com.example.android_project_bike

import com.google.firebase.firestore.GeoPoint
import java.lang.ref.Reference
import java.sql.Timestamp

data class Ride (

    //val bike: Reference<Bike>,
    val start_time: Timestamp,
    val end_time: Timestamp,
    val route: Array<GeoPoint>,
    val total_km: Double,
    val total_price: Double
){

}

