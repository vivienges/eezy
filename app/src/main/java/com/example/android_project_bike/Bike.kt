package com.example.android_project_bike

import com.google.firebase.firestore.GeoPoint

data class Bike (
    val id: Int,
    var charge: Int,
    var available: Boolean,
    var locked: Boolean,
    var position: GeoPoint
)

{
    constructor() : this(0, 0, true, true, GeoPoint(0.0, 0.0)) {

    }


    //override fun toString() = "Bike " + id
}