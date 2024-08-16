package com.example.pantrymanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.username)
        password = findViewById(R.id.password)
        loginButton = findViewById(R.id.login)
        firebaseAuth = FirebaseAuth.getInstance()
        loginButton.setOnClickListener { signInWithEmailAndPassword() }

        if (firebaseAuth.currentUser != null) { //skips login if user hasn't log out
            finish()
            var myIntent: Intent = Intent(this, PantryActivity::class.java)
            startActivity(myIntent)
        }

        var registerB : TextView = findViewById(R.id.register)
        registerB.setOnClickListener{
            finish()
            var intent : Intent = Intent(this,RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signInWithEmailAndPassword() {
        val emailString = email.text.trim().toString()
        val password = password.text.trim().toString()

        firebaseAuth.signInWithEmailAndPassword(emailString, password)
            .addOnCompleteListener { loginTask ->
                if (loginTask.isSuccessful) {
                    finish()
                    val myIntent = Intent(this, PantryActivity::class.java)
                    startActivity(myIntent)
                } else {
                    if (loginTask.exception is FirebaseAuthInvalidUserException ||
                        loginTask.exception is FirebaseAuthInvalidCredentialsException
                    ) {
                        Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Login failed: ${loginTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }


}