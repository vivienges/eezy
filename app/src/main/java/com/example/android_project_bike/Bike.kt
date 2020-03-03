package com.example.android_project_bike

data class Bike(
    val id: Int,
    var battery: Int,
    var available: Boolean,
    var longitude: Double,
    var latitude: Double

)
{
    override fun toString() = "Bike " + id
}