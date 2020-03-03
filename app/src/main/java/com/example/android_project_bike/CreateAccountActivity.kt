package com.example.android_project_bike

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_create_account.*

class CreateAccountActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        val editEmail = findViewById<EditText>(R.id.email)
        val addPaymentButton = findViewById<Button>(R.id.addPayment)
        val editPassword = findViewById<EditText>(R.id.password)
        val passwordConfirmation = findViewById<EditText>(R.id.passwordConfirmation)


        editEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {


                val emailInput = editEmail.editableText.toString().trim()

                if (emailInput.isEmpty()) {
                    editEmail.setError("Field can't be empty")
                }

                else if(!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                    editEmail.setError("Input is not a valid email")
                }
                else {
                    editEmail.setError(null)



                                    addPaymentButton.isEnabled = true
                                }
                        }

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                        }

                    })


        val addPayment = findViewById<Button>(R.id.addPayment)

        addPayment.setOnClickListener {

            val intent = Intent(this, AddPaymentActivity::class.java)
            startActivity(intent)
        }
    }
}
