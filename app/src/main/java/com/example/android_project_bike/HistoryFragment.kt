package com.example.android_project_bike

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore


class HistoryFragment : Fragment() {

    private var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    lateinit var adapter: ArrayAdapter<String>
    lateinit var user: User
    var dataReceived = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_history, container, false)


        val listView = view.findViewById<ListView>(R.id.history_list)
        val listItems = mutableListOf<String>()
        adapter = ArrayAdapter(
            context!!,
            android.R.layout.simple_list_item_1,
            listItems
        )

        listView.adapter = adapter


        auth = FirebaseAuth.getInstance()

        db.collection("users").document(auth.currentUser!!.uid)
            .addSnapshotListener { snapshot, e ->

                if (e != null) {
                    Log.w("FAIL", "Listen failed.", e)
                    return@addSnapshotListener
                }


                if (snapshot != null && snapshot.exists()) {
                    Log.d("DATA", "Current data: ${snapshot.data}")
                    user = snapshot.toObject(User::class.java)!!

                    for (entry in user.history) {

                        var startTime = ""
                        val ride = entry.get().addOnSuccessListener { result ->
                            startTime = result["total_km"].toString()
                        }

                        listItems.add(startTime)

                    }

                } else {
                    Log.d("NULL", "Current data: null")
                }
                adapter.notifyDataSetChanged()
            }

        Log.d("TAG", "Fuuuuuuuuu")
        return view

    }
}
