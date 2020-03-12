package com.example.android_project_bike

import com.google.firebase.firestore.GeoPoint

data class User (

    val email: String,
   // var history: Array<Ride>,
    var password: String,
    var payment: Int // e.g. Paypal = 1, Credit Card = 2, Google Pay = 3
)

{
    constructor() : this("", "", 1) {
    }}