package com.example.android_project_bike

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text


class ProfileFragment : Fragment() {

    private var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        val email = view.findViewById<TextView>(R.id.email_value)
        val payment = view.findViewById<TextView>(R.id.payment_value)

        db.collection(USERS).document(currentUser!!.uid)
            .addSnapshotListener { snapshot, e ->

                if (e != null) {
                    Log.w("FAIL", "Listen failed.", e)
                    return@addSnapshotListener
                }


                if (snapshot != null && snapshot.exists()) {
                    Log.d("DATA", "Current data: ${snapshot.data}")
                    user = snapshot.toObject(User::class.java)!!

                    email.text = user.email

                    when(user.payment) {
                        1 -> payment.text = resources.getString(R.string.paypal)
                        2 -> payment.text = resources.getString(R.string.credit_card)
                        3 -> payment.text = resources.getString(R.string.googlepay)
                    }

                } else {
                    Log.d("NULL", "Current data: null")
                }

            }



        return view
    }

    companion object {
        const val USERS = "users"
    }

}
