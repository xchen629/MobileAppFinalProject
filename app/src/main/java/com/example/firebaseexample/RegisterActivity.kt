package com.example.firebaseexample

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    // Just a number for activity for results
    private val SIGN_IN_REQUEST_CODE = 123

    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // If currentUser is not null, we have a user and go back to the MainActivity
        if (FirebaseAuth.getInstance().currentUser != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            // Make sure to call finish(), otherwise the user would be able to go back to the RegisterActivity
            finish()
        }
    }


    fun loginButton(view: View) {


        // Choose authentication providers -- make sure enable them on your firebase account first
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
            //AuthUI.IdpConfig.PhoneBuilder().build(),
            //AuthUI.IdpConfig.FacebookBuilder().build(),
            //AuthUI.IdpConfig.TwitterBuilder().build()
        )

        // Create  sign-in intent
        val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setTosAndPrivacyPolicyUrls("https://example.com", "https://example.com")
            .setLogo(R.drawable.ic_contacts_black_24dp)
            .setAlwaysShowSignInMethodScreen(true) // use this if you have only one provider and really want the see the signin page
            .setIsSmartLockEnabled(false)
            .build()

        // launch the sign-in intent above
        startActivityForResult(intent, SIGN_IN_REQUEST_CODE)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // The user has successfully signed in or he/she is a new user

                val user = FirebaseAuth.getInstance().currentUser
                Log.d(TAG, "onActivityResult: $user")

                //Checking for User (New/Old)
                if (user?.metadata?.creationTimestamp == user?.metadata?.lastSignInTimestamp) {
                    //This is a New User
                    Toast.makeText(this, "Welcome New User!", Toast.LENGTH_SHORT).show()
                } else {
                    //This is a returning user
                    Toast.makeText(this, "Welcome Back!", Toast.LENGTH_SHORT).show()
                }


                // Since the user signed in, the user can go back to main activity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                // Make sure to call finish(), otherwise the user would be able to go back to the RegisterActivity
                finish()

            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.

                val response = IdpResponse.fromResultIntent(data)
                if (response == null){
                    Log.d(TAG, "onActivityResult: the user has cancelled the sign in request")
                }
                else{
                    Log.e(TAG, "onActivityResult: ${response.error?.errorCode}")
                }
            }
        }
    }
}
