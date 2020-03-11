package com.example.android_project_bike

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Document

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val editMail = findViewById<EditText>(R.id.mail_input)
        val addPaymentButton = findViewById<Button>(R.id.add_payment_button)
        val editPassword = findViewById<EditText>(R.id.password_input)
        val passwordConfirmation = findViewById<EditText>(R.id.confirmed_password_input)


        //TODO: Check if email already exists
        //TODO: Check if password is the same
        //TODO: Hash password

        editMail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {


                val emailInput = editMail.editableText.toString().trim()

                if (emailInput.isEmpty()) {
                    editMail.setError("Field can't be empty")
                }

                else if(!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                    editMail.setError("Input is not a valid email")
                }
                else {
                    editMail.setError(null)



                                    addPaymentButton.isEnabled = true
                                }
                        }

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                        }

                    })


        val addPayment = findViewById<Button>(R .id.add_payment_button)

        addPayment.setOnClickListener {

            auth.createUserWithEmailAndPassword(editMail.text.toString(), editPassword.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("SUCCESS", "createUserWithEmail:success")
                        val user = auth.currentUser
                        val historyDocument = db
                            .collection("histories")
                            .document()
                        historyDocument
                            .set(hashMapOf(
                                "history" to emptyList<DocumentReference>()
                            ))
                        val userDocument = db
                            .collection("users")
                            .document(user!!.uid)
                        val userInfo = hashMapOf(
                            "mail" to user.email,
                            "payment" to 0,
                            "history" to historyDocument.id
                        )
                        userDocument.set(userInfo)
                            .addOnSuccessListener {result ->
                                Log.d("SUCCESS", "Added $result")
                            }
                            .addOnFailureListener{ exception ->
                                Log.d("ERROR", "Adding data failed!")
                            }
                        val intent = Intent(this, AddPaymentActivity::class.java)
                        startActivity(intent)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("ERROR", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }

                    // ...
                }
        }
    }
}
