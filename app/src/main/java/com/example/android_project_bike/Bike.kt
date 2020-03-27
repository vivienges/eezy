package com.example.android_project_bike

import com.google.firebase.firestore.GeoPoint

data class Bike (
    var id: Int,
    var charge: Int,
    var available: Boolean,
    var locked: Boolean,
    var reserved: Boolean,
    var position: GeoPoint,
    var current_user: String
)

{
    constructor() : this(0, 0, true, true, false, GeoPoint(0.0, 0.0), "") {

    }

}