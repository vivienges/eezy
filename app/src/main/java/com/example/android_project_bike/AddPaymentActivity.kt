package com.example.android_project_bike

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference


class AddPaymentActivity : AppCompatActivity() {

    private var db = FirebaseFirestore.getInstance()
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_payment)

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)


        val intent = intent
        val bundle = intent.getBundleExtra("bundle")
        val email = bundle.getString("email")
        val password = bundle.getString("password")

        auth = FirebaseAuth.getInstance()


        val confirmSignup = findViewById<Button>(R.id.confirmSignup)

        confirmSignup.setOnClickListener {

            val radioButton = radioGroup.checkedRadioButtonId

            var payment = 0
            val list = emptyList<DocumentReference>()

            when (radioButton) {
                R.id.radio_1 -> payment = 1
                R.id.radio_2 -> payment = 2
                R.id.radio_3 -> payment = 3
            }

            var user = User("$email", "$password", payment, emptyList<DocumentReference>())

            auth.createUserWithEmailAndPassword(user.email, user.password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("SUCCESS", "createUserWithEmail:success")
                        val user = auth.currentUser
                        val userDocument = db
                            .collection("users")
                            .document(user!!.uid)
                        val userInfo = hashMapOf(
                            "email" to user.email,
                            "payment" to payment,
                            "history" to emptyList<DocumentReference>()
                        )
                        userDocument.set(userInfo)
                            .addOnSuccessListener { result ->
                                Log.d("SUCCESS", "Added $result")
                            }
                            .addOnFailureListener { exception ->
                                Log.d("ERROR", "Adding data failed!")
                            }
                        val intent = Intent(this, MainActivity::class.java)
                        //intent.putExtra("bundle", bundle)
                        startActivity(intent)
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("ERROR", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

        }
    }
}
