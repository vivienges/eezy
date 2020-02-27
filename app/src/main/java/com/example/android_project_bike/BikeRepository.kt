package com.example.android_project_bike

val bikeRepository = BikeRepository().apply {

    addBike(
        100,
        true,
        longitude = 23.4312,
        latitude = 20.3232


    )

    addBike(
        84,
        false,
        longitude = 75.3212,
        latitude = 45.2365
    )
}

class BikeRepository {

    private val bikes = mutableListOf<Bike>()

    fun addBike(battery: Int, available: Boolean, longitude: Double, latitude: Double): Int{
        val id = when{
            bikes.count() == 0 -> 1
            else -> bikes.last().id+1
        }
        bikes.add(Bike(
            id,
            battery,
            available,
            longitude,
            latitude
        ))
        return id
    }

    fun getAllBikes() = bikes

    fun getToDoById(id: Int) =
        bikes.find {
            it.id == id
        }

    fun deleteToDoById(id: Int) =
        bikes.remove(
            bikes.find {
                it.id == id
            }
        )

    fun updateToDoById(id: Int, newBattery: Int, newAvailable: Boolean, newLongitude: Double, newLatitude: Double) {

        getToDoById(id)?.run {
            battery = newBattery
            available = newAvailable
            longitude = newLongitude
            latitude = newLatitude
        }
    }
}