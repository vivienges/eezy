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


class LoginActivity : BaseActivity() {

    val RC_SIGN_IN: Int = 1
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mGoogleSignInOptions: GoogleSignInOptions
    private lateinit var auth: FirebaseAuth
    private var loggedIn = false

    lateinit var bundle : Bundle


    // private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        bundle = intent.getBundleExtra("bundle")



        val login_button = findViewById<Button>(R.id.login_button)

        login_button.setOnClickListener() {

            var email = findViewById<EditText>(R.id.email_login)

            var password = findViewById<EditText>(R.id.password_login)

            auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnCompleteListener(this){ task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("SUCCESS", "signInWithEmail:success")
                        val user = auth.currentUser
                        loggedIn = user != null

                        val intent = Intent(this, BikeDetailsActivity::class.java)
                        intent.putExtra("bundle", bundle)
                        startActivity(intent)
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("FAIL", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }

                }


        }


        val googleSignIn = findViewById<SignInButton>(R.id.sign_in_button)

        val textView = googleSignIn.getChildAt(0) as TextView
        textView.text = "LOGIN"


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
            intent.putExtra("bundle", bundle)
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
                Toast.makeText(this, "Welcome, ${account.displayName}", Toast.LENGTH_LONG).show()

                val user = auth.currentUser
                loggedIn = user != null

                val intent = Intent(this, BikeDetailsActivity::class.java)
                intent.putExtra("bundle", bundle)
                startActivity(intent)
                finish()

            } catch (e: ApiException) {
                Log.d("ERROR", e.toString())
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
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
                    if (task.result!!.additionalUserInfo!!.isNewUser)
                    //TODO: Add user info to database
                        Log.d("INFO", "New user created: " + acct.displayName )
                    //TODO: Redirect user to main activity
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("ERROR", "signInWithCredential:failure", task.exception)
                }
            }
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




