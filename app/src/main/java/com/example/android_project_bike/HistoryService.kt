package com.example.android_project_bike

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.ArrayAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryService : Service() {

    private var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    lateinit var adapter: ArrayAdapter<String>
    lateinit var user: User

    override fun onCreate() {
        super.onCreate()

    }
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
