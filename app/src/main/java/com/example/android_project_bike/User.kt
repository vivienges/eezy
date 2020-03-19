package com.example.android_project_bike

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint

data class User (

    val email: String,
    var password: String,
    var payment: Int, // e.g. Paypal = 1, Credit Card = 2, Google Pay = 3
    var history: List<DocumentReference>
)

{
    constructor() : this("", "", 1, emptyList<DocumentReference>()) {
    }}