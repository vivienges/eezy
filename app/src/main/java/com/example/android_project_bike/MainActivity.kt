package com.example.android_project_bike

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.multidex.MultiDex
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    // Set up multidex for this activity
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //TO BE DELETED!
        /* !- Sample code for adding user to database! -!

        val db = FirebaseFirestore.getInstance()
        // Create a new user
        val user = hashMapOf(
            "mail" to "user@lab.com",
            "password" to "1234",
            "payment_name" to "Paypal"
        )

        // Add a new document with a generated ID
        db.collection("users")
            .add(user as Map<String, Any>)
            .addOnSuccessListener { documentReference ->
                Log.d("SUCCESS", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("FAILURE", "Error adding document", e)
            }
         */
    }
}
