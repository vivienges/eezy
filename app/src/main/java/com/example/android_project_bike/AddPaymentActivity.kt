package com.example.android_project_bike

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference


class AddPaymentActivity : AppCompatActivity() {

    private var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_payment)

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val intent = intent
        val bundle = intent.getBundleExtra(BUNDLE)
        val email = bundle.getString(EMAIL)
        val password = bundle.getString(PASSWORD)

        auth = FirebaseAuth.getInstance()


        val confirmSignup = findViewById<Button>(R.id.confirmSignup)

        confirmSignup.setOnClickListener {

            val radioButton = radioGroup.checkedRadioButtonId
            var payment = 0

            when (radioButton) {
                R.id.radio_1 -> payment = 1
                R.id.radio_2 -> payment = 2
                R.id.radio_3 -> payment = 3
            }

            if (auth.currentUser != null) {

                db.collection(USERS).document(auth.currentUser!!.uid)
                    .update(
                        mapOf(
                            PAYMENT to payment
                        )
                    )
                    .addOnSuccessListener { result ->
                        Log.d("SUCCESS", "Added $result")
                    }
                    .addOnFailureListener { exception ->
                        Log.d("ERROR", "Adding data failed!")
                    }

                val intent = Intent(this, EnterQrCodeActivity::class.java)
                intent.putExtra(BUNDLE, bundle)
                startActivity(intent)
                finish()
            }

            else {

                val user = User("$email", "$password", payment, emptyList<DocumentReference>())

                auth.createUserWithEmailAndPassword(user.email, user.password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SUCCESS", "createUserWithEmail:success")
                            val currentUser = auth.currentUser
                            val userDocument = db
                                .collection(USERS)
                                .document(currentUser!!.uid)
                            val userInfo = hashMapOf(
                                EMAIL to currentUser.email,
                                PAYMENT to payment,
                                HISTORY to emptyList<DocumentReference>()
                            )
                            userDocument.set(userInfo)
                                .addOnSuccessListener { result ->
                                    Log.d("SUCCESS", "Added $result")
                                }
                                .addOnFailureListener { exception ->
                                    Log.d("ERROR", "Adding data failed!")
                                }
                            Toast.makeText(this, "Welcome, ${currentUser!!.email}", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, MainActivity::class.java)
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
    companion object {
        const val BUNDLE = "bundle"
        const val EMAIL = "email"
        const val PASSWORD = "password"
        const val PAYMENT = "payment"
        const val HISTORY = "history"
        const val USERS = "users"
    }
}
