package com.example.android_project_bike

import android.provider.ContactsContract

val userRepository = UserRepository().apply {

    addUser(
        email = "vivien-geschwind@live.de",
        password ="abc123",
        payment = 1
    )

    addUser(
        email = "test@example.com",
        password = "test123",
        payment = 2
    )
}

class UserRepository {

    private val users = mutableListOf<User>()

    fun addUser(email: String, password: String, payment: Int): Int{
        val id = when{
            users.count() == 0 -> 1
            else -> users.last().id+1
        }
        users.add(User(
            id,
            email,
            password,
            payment
        ))
        return id
    }

    fun getAllUser() = users

    fun getUserById(id: Int) =
        users.find {
            it.id == id
        }

    /* fun deleteToDoById(id: Int) =
        bikes.remove(
            bikes.find {
                it.id == id
            }
        ) */

    fun updateBike(id: Int, newEmail: String, newPassword: String, newPayment: Int) {

        getUserById(id)?.run {
            email = newEmail
            password = newPassword
            payment = newPayment

        }
    }
}