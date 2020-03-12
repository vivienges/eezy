package com.example.android_project_bike

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.multidex.MultiDex
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern

class CreateAccountActivity : AppCompatActivity() {

    private var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    var userEmails = mutableListOf<String>()
    lateinit var addPaymentButton: View
    var email = ""
    var password = ""
    var emailValidated = false
    var passwordValidated = false
    var passwordConfirmationValidated = false


    var PASSWORD_PATTERN = Pattern.compile(
        "^" +
                "(?=.*[0-9])" +
                "(?=.*[a-z])" +
                "(?=.*[A-Z])" +
                // "(?=.*[@#$^&+=!?])" +
                "(?=\\S+$)" +
                ".{6,}" +
                "$"
    )

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        auth = FirebaseAuth.getInstance()

        val intent = intent
        val bundle = intent.getBundleExtra("bundle")




        val editEmail = findViewById<EditText>(R.id.email_input)
        addPaymentButton = findViewById<Button>(R.id.add_payment_button)
        val editPassword = findViewById<EditText>(R.id.password_input)
        val passwordConfirmation = findViewById<EditText>(R.id.confirmed_password_input)


        //TODO: Check if email already exists
        //TODO: Check if password is the same
        //TODO: Hash password

        editEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {


                var emailInput = editEmail.editableText.toString().trim()

                if (emailInput.isEmpty()) {
                    editEmail.setError("Field can't be empty")
                    emailValidated = false
                } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                    editEmail.setError("Please enter a valid Email address")
                    emailValidated = false
                } else if (emailExists(emailInput)) {
                    editEmail.setError("email is already used")

                } else {
                    editEmail.setError(null)
                    email = emailInput
                    emailValidated = true
                    bundle.putString("email", email)
                }

                validateInput()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })


        editPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {


                val passwordInput = editPassword.editableText.toString().trim()

                if (passwordInput.isEmpty()) {
                    editPassword.setError("Field can't be empty")
                    passwordValidated = false
                } else if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
                    editPassword.setError("Password too weak")
                    passwordValidated = false
                } else {
                    editPassword.setError(null)
                    passwordValidated = true
                }

                validateInput()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }



        })

        passwordConfirmation.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {


                val passwordConfirmationInput = passwordConfirmation.editableText.toString().trim()

                if (passwordConfirmationInput.isEmpty()) {
                    passwordConfirmation.setError("Field can't be empty")
                    passwordConfirmationValidated = false
                } else if (passwordConfirmationInput != editPassword.editableText.toString().trim()) {
                    passwordConfirmation.setError("Password is not the same")
                    passwordConfirmationValidated = false
                } else {
                    passwordConfirmation.setError(null)
                    password = passwordConfirmationInput
                    passwordConfirmationValidated = true
                    bundle.putString("password", password)


                }

                validateInput()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }



        })


        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    userEmails.add("${document.id}")

                }
            }
            .addOnFailureListener { exception ->
                Log.w("FAILURE", "Error getting documents: ", exception)
            }




        addPaymentButton.setOnClickListener {

            val intent = Intent(this, AddPaymentActivity::class.java)
            intent.putExtra("bundle", bundle)
            startActivity(intent)
        }


    }

    private fun emailExists(email: String): Boolean {

        for (emails in userEmails) {
            if (emails == email) {
                return true
            }
        }

        return false
    }

    private fun validateInput() {
        if (emailValidated && passwordValidated && passwordConfirmationValidated) {
            addPaymentButton.isEnabled = true

        }

        else {
            addPaymentButton.isEnabled = false
        }
        Log.d("Check", "$emailValidated + $passwordValidated + $passwordConfirmationValidated")
    }

}




