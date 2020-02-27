package com.example.android_project_bike

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlinx.android.synthetic.main.activity_login.*
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.google.android.gms.auth.api.signin.GoogleSignInClient


class LoginActivity : AppCompatActivity() {

    val RC_SIGN_IN: Int = 1
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mGoogleSignInOptions: GoogleSignInOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        val googleSignIn = findViewById<Button>(R.id.sign_in_button)




            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("406639512806-7e01k24vopkh3sv7346r8v73mp2l3tqj.apps.googleusercontent.com")
                .requestEmail()
                .build()

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


        fun setupUI() {
            googleSignIn.setOnClickListener {

            }
        }


        val createAccount = findViewById<Button>(R.id.createAccount)

        createAccount.setOnClickListener {

            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }



        }

    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


/*    override fun onStart() {
        super.onStart()

        // Check for existing Google Sign In account, if the user is already signed in
// the GoogleSignInAccount will be non-null.
        val account = GoogleSignIn.getLastSignedInAccount(this)
       // updateUI(account)
    }

*/
}




