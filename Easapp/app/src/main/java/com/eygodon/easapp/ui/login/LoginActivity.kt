package com.eygodon.easapp.ui.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast

import com.eygodon.easapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val login = findViewById<Button>(R.id.login)
        val register = findViewById<Button>(R.id.register)
        val loading = findViewById<ProgressBar>(R.id.loading)

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
                .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid
            register.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                        username.text.toString(),
                        password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                                username.text.toString(),
                                password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                signIn(username.text.toString(), password.text.toString())
            }

            register.setOnClickListener {
                createUserAccount(username.text.toString(), password.text.toString())
            }
        }
    }


    override fun onStart()
    {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null) {
            loginSuccessfulActivity()
            finish()
        }
    }
    private fun loginSuccessfulActivity()
    {
        val switchIntent : Intent = Intent(this, ProfileActivity::class.java )
        startActivity(switchIntent)
    }

    private fun createUserAccount (mail: String, pass : String)
    {
               auth.createUserWithEmailAndPassword(mail, pass).addOnCompleteListener(this)
               { task ->
                   if (task.isSuccessful) {
                       val user = auth.currentUser
                       Log.d("accountCreation","account created successfully")
                       updateUiWithUser(user)
                   } else {
                       Log.d("accountCreation", "error encountered")
                       updateUiWithUser(null)
                   }
               }
    }

    private fun signIn(mail : String, pass : String)
    {
        auth.signInWithEmailAndPassword(mail, pass).
                addOnCompleteListener(this) {
                    task ->
                    if (task.isSuccessful)
                    {
                        Log.d("connect", "Signed in successfully")
                        val user = auth.currentUser
                        updateUiWithUser(user)
                    }
                    else {
                        Log.d("connect","Failure signing in")
                        updateUiWithUser(null)
                    }
                }
    }
    private fun updateUiWithUser(user: FirebaseUser?) {
        val welcome = getString(R.string.welcome)
        if (user == null)
            Toast.makeText(applicationContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
        else {
            val email = user.email
            Toast.makeText(
                    applicationContext,
                    "$welcome $email",
                    Toast.LENGTH_LONG
                    ).show()
            loginSuccessfulActivity()
        }
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}