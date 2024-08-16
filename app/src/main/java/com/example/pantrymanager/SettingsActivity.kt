package com.example.pantrymanager

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {
    companion object {
        var isDarkMode : Boolean = false
        var ISDARK : String = "isdark"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //local light/dark mode preferences
        var sp : SharedPreferences = this.getSharedPreferences("xyz", Context.MODE_PRIVATE)

        isDarkMode = sp.getBoolean(ISDARK, false)
        Log.w("MA", "received: " + isDarkMode)

        //SETUP logout button
        var logoutB : Button = findViewById<Button>(R.id.logout)
        logoutB.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            var intent : Intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
        //SETUP light_dark mode button
        var lightdarkB : Button = findViewById<Button>(R.id.light_dark)

        lightdarkB.setOnClickListener{
            if (isDarkMode) {
                //change to light mode!
                isDarkMode = false
                Log.w("MA", "changed to light mode!")

                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                //change to dark mode!
                isDarkMode = true
                Log.w("MA", "changed to dark mode!")

                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
            }


            //set preferences
            sp.edit().putBoolean(ISDARK, isDarkMode).commit()
        }
        //SETUP exit settings button
        var exitB : Button = findViewById<Button>(R.id.exit)
        exitB.setOnClickListener{
            //go to the last view
            finish()
            Log.w(GroceryActivity.MA, ":)")
            var intent : Intent = Intent(this,PantryActivity::class.java)
            startActivity(intent)
        }

    }

}