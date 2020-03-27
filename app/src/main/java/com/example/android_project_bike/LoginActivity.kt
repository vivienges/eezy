package com.example.android_project_bike

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore


class LoginActivity : AppCompatActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var finishReceiver: BroadcastReceiver
    private var db = FirebaseFirestore.getInstance()
    private val RC_SIGN_IN: Int = 1
    private var loggedIn = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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

        val loginButton = findViewById<Button>(R.id.login_button)

        loginButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.email_login)
            val password = findViewById<EditText>(R.id.password_login)

            auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("SUCCESS", "signInWithEmail:success")
                        val currentUser = auth.currentUser
                        loggedIn = currentUser != null
                        Toast.makeText(
                            this,
                            "${resources.getString(R.string.welcome)}, ${currentUser!!.email}",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("FAIL", "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext, resources.getString(R.string.authentication_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }


        val googleSignIn = findViewById<SignInButton>(R.id.sign_in_button)
        val textView = googleSignIn.getChildAt(0) as TextView
        textView.text = resources.getString(R.string.login_google)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignIn.setOnClickListener {
            signIn()
        }

        val createAccount = findViewById<Button>(R.id.createAccount)

        createAccount.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                Log.d("ERROR", e.toString())
                Toast.makeText(
                    this,
                    resources.getString(R.string.google_signin_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("INFO", "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("SUCCESS", "signInWithCredential:success")
                    if (task.result!!.additionalUserInfo!!.isNewUser) {
                        val user = auth.currentUser
                        val userDocument = db
                            .collection(USERS)
                            .document(user!!.uid)
                        val userInfo = hashMapOf(
                            EMAIL to user.email,
                            PAYMENT to 0,
                            HISTORY to emptyList<DocumentReference>()
                        )
                        userDocument.set(userInfo)
                            .addOnSuccessListener { result ->
                                Log.d("SUCCESS", "Added $result")
                            }
                            .addOnFailureListener {
                                Log.d("ERROR", "Adding data failed!")
                            }

                        val intent = Intent(this, AddPaymentActivity::class.java)
                        startActivity(intent)
                        Log.d("INFO", "New user created: " + acct.displayName)
                    }
                    Toast.makeText(
                        this,
                        "${resources.getString(R.string.welcome)}, ${acct.displayName}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("ERROR", "signInWithCredential:failure", task.exception)
                }
            }
    }

    companion object {
        const val FINISH_LOGIN_ACTIVITIES = "finish_login_activities"
        const val USERS = "users"
        const val EMAIL = "email"
        const val PAYMENT = "payment"
        const val HISTORY = "history"
    }
}




