package com.example.android_project_bike

data class User (

    val id: Int,
    var email: String,
    var password: String,
    var payment: Int // e.g. Paypal = 1, Credit Card = 2, Google Pay = 3

)