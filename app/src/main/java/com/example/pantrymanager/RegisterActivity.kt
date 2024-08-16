package com.example.pantrymanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var registerButton: Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        email = findViewById(R.id.username)
        password = findViewById(R.id.password)
        registerButton = findViewById(R.id.register)
        firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth.currentUser != null) {
            FirebaseAuth.getInstance().signOut()
        }

        registerButton.setOnClickListener { registration() }

        var loginB : TextView = findViewById(R.id.login)
        loginB.setOnClickListener{
            finish()
            var intent : Intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun registration() {
        val emailString = email.text.trim().toString()
        val password = password.text.trim().toString()

        firebaseAuth.createUserWithEmailAndPassword(emailString, password)
            .addOnCompleteListener { registrationTask ->
                if (registrationTask.isSuccessful) {
                    finish()
                    val myIntent = Intent(this, PantryActivity::class.java)
                    startActivity(myIntent)
                } else {
                    Toast.makeText(this, "Registration failed: ${registrationTask.exception?.message}", Toast.LENGTH_SHORT).show()
                }

            }
    }
}