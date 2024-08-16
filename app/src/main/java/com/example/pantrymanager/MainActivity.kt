package com.example.pantrymanager

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.ads.AdView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    companion object {
        var pantry : ItemList = ItemList()
        var grocery : ItemList = ItemList()
        var fireBase: FirebaseDatabase = FirebaseDatabase.getInstance()
        var fireAuth : FirebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var login : Button = findViewById(R.id.login)
        login.setOnClickListener{goToLogin()}

        var register : Button = findViewById(R.id.register)
        register.setOnClickListener{goToRegister()}

        var sp : SharedPreferences = this.getSharedPreferences("xyz", Context.MODE_PRIVATE)
        var isDarkMode = sp.getBoolean(SettingsActivity.ISDARK, false)

        SettingsActivity.isDarkMode = isDarkMode

        Log.w("MainActivity", "isDarkMode HERE : "+isDarkMode)

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO);
        }


    }

    fun goToLogin() {
        var myIntent : Intent = Intent(this,LoginActivity::class.java)
        startActivity(myIntent)
    }

    fun goToRegister() {
        var myIntent : Intent = Intent(this,RegisterActivity::class.java)
        startActivity(myIntent)
    }

}