package com.example.android_project_bike

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import android.widget.EditText

class CreateAccountActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

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


        val addPayment = findViewById<Button>(R.id.add_payment_button)

        addPayment.setOnClickListener {

            val intent = Intent(this, AddPaymentActivity::class.java)
            startActivity(intent)
        }
    }
}
