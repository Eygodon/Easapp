package com.eygodon.easapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.eygodon.easapp.R
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(findViewById(R.id.toolbar))

        auth = FirebaseAuth.getInstance()


        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            logout()
        }
    }

    private fun signInActivity()
    {
        val switchIntent : Intent = Intent(this, LoginActivity::class.java)
        startActivity(switchIntent)
    }

    private fun logout ()
    {
        auth.signOut()
        if (auth.currentUser == null)
            Log.d("Logging out","Signed out successfully")
        else
            Log.d("Logging out", "Not correctly logged out")
        signInActivity()
    }
}