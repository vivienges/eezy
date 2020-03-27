package com.example.android_project_bike

import com.google.firebase.firestore.GeoPoint
import java.lang.ref.Reference
import java.sql.Timestamp
import java.util.*

data class Ride (

    //val bike: Reference<Bike>,
    val start_time: Date,
    val end_time: Date,
    val route: List<GeoPoint>,
    val total_km: Double,
    val total_price: Double
){

    constructor() : this(
        (Date(Date().time)),
        (Date(Date().time)),
        List(2) { GeoPoint(0.0, 0.0); GeoPoint(0.0, 0.0) },
        0.0,
        0.0
    )

}

