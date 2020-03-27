package com.example.android_project_bike

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.multidex.MultiDex
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var addPaymentButton: View
    private lateinit var finishReceiver: BroadcastReceiver
    private var db = FirebaseFirestore.getInstance()
    private var userEmails = mutableListOf<String>()
    private var email = ""
    private var password = ""
    private var emailValidated = false
    private var passwordValidated = false
    private var passwordConfirmationValidated = false
    private var PASSWORD_PATTERN = Pattern.compile(
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

        finishReceiver = object : BroadcastReceiver() {
            override fun onReceive(arg0: Context, intent: Intent) {
                val action = intent.action
                if (action == FINISH_LOGIN_ACTIVITIES) {
                    finish()
                }
            }
        }
        registerReceiver(finishReceiver, IntentFilter(FINISH_LOGIN_ACTIVITIES))

        auth = FirebaseAuth.getInstance()
        val bundle = Bundle()

        val editEmail = findViewById<EditText>(R.id.email_input)
        addPaymentButton = findViewById<Button>(R.id.add_payment_button)
        val editPassword = findViewById<EditText>(R.id.password_input)
        val passwordConfirmation = findViewById<EditText>(R.id.confirmed_password_input)

        editEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val emailInput = editEmail.editableText.toString().trim()

                if (emailInput.isEmpty()) {
                    editEmail.error = resources.getString(R.string.field_empty_error)
                    emailValidated = false
                } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                    editEmail.error = resources.getString(R.string.mail_invalid_error)
                    emailValidated = false
                } else if (emailExists(emailInput)) {
                    editEmail.error = resources.getString(R.string.mail_duplicate_error)

                } else {
                    editEmail.error = null
                    email = emailInput
                    emailValidated = true
                    bundle.putString(EMAIL, email)
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
                    editPassword.error = resources.getString(R.string.field_empty_error)
                    passwordValidated = false
                } else if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
                    editPassword.error = resources.getString(R.string.password_weak_error)
                    passwordValidated = false
                } else {
                    editPassword.error = null
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
                    passwordConfirmation.error = resources.getString(R.string.field_empty_error)
                    passwordConfirmationValidated = false
                } else if (passwordConfirmationInput != editPassword.editableText.toString().trim()) {
                    passwordConfirmation.error = resources.getString(R.string.password_mismatch_error)
                    passwordConfirmationValidated = false
                } else {
                    passwordConfirmation.error = null
                    password = passwordConfirmationInput
                    passwordConfirmationValidated = true
                    bundle.putString(PASSWORD, password)
                }
                validateInput()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        db.collection(USERS)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    userEmails.add(document.id)

                }
            }
            .addOnFailureListener { exception ->
                Log.w("FAILURE", "Error getting documents: ", exception)
            }

        addPaymentButton.setOnClickListener {
            val intent = Intent(this, AddPaymentActivity::class.java)
            intent.putExtra(BUNDLE, bundle)
            startActivity(intent)
            finish()
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
        addPaymentButton.isEnabled = emailValidated && passwordValidated && passwordConfirmationValidated
        Log.d("Check", "$emailValidated + $passwordValidated + $passwordConfirmationValidated")
    }

    companion object {
        const val FINISH_LOGIN_ACTIVITIES = "finish_login_activities"
        const val BUNDLE = "bundle"
        const val USERS = "users"
        const val EMAIL = "email"
        const val PASSWORD = "password"
    }

}




