package com.example.pantrymanager

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.w3c.dom.Text

class GroceryActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var adapter: CustomAdapter
    private lateinit var grocery : ItemList
    private lateinit var adView : AdView
    private lateinit var fireBase : FirebaseDatabase
    private lateinit var fireAuth : FirebaseAuth
    private var oneChecked : Boolean = false

    private lateinit var addLayout : LinearLayout
    private lateinit var quanLayout : LinearLayout
    private lateinit var confirmB : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grocery)

        listView = findViewById(R.id.grocery_data)

        grocery = MainActivity.grocery
        fireBase = MainActivity.fireBase
        fireAuth = MainActivity.fireAuth
        readDatabase()

        //set up the button
        var confirm : Button = findViewById(R.id.add_button)
        var handler = AddButtonHandler()
        confirm.setOnClickListener(handler)
        adapter = CustomAdapter(grocery.getItems(), applicationContext)
        listView.adapter = adapter

        // create an AdView
        adView = AdView( this )
        var adSize : AdSize = AdSize( AdSize.FULL_WIDTH, AdSize.AUTO_HEIGHT )
        adView.setAdSize( adSize )
        var adUnitId : String = "ca-app-pub-3940256099942544/6300978111"
        adView.adUnitId = adUnitId
        // create an AdRequest
        var builder : AdRequest.Builder = AdRequest.Builder( )
        builder.addKeyword( "food" ).addKeyword( "cooking" )
        var request : AdRequest = builder.build()
        // put the AdView in the LinearLayout
        var adLayout : LinearLayout = findViewById( R.id.ad_view )
        adLayout.addView( adView )
        // load the ad
        adView.loadAd( request )

        var settings : ImageButton = findViewById(R.id.home)
        settings.setOnClickListener{goSettings()}

        var pantry : ImageButton = findViewById(R.id.go_to_pantry)
        pantry.setOnClickListener{goPantry()}
    }

    fun goSettings() {
        finish()
        var intent : Intent = Intent(this,SettingsActivity::class.java)
        startActivity(intent)
    }

    fun goPantry() {
        finish()
        var intent : Intent = Intent(this,PantryActivity::class.java)
        startActivity(intent)
    }

    inner class AddButtonHandler : View.OnClickListener {
        override fun onClick(v: View?) {
            Log.w(MA, "clicked!")
            var item = findViewById<EditText>(R.id.add_et)
            var q = findViewById<EditText>(R.id.quantity_et)
            if (item != null && q != null && item.text.toString() != "" && q.text.toString()!="") {
                //add the item into the pantry and display list again
                var i : String = item.text.toString()
                //Log.w(MA, "item: "+i)
                var qu : Int = q.text.toString().toInt()
                //Log.w(MA, "quantity: "+qu)
                grocery.addItem(i, qu)
                grocery.sort()
                vibrate()
                updateDatabase()
                adapter.notifyDataSetChanged()
                item.setText("")
                q.setText("")
            }
        }
    }

    fun readDatabase() {
        grocery.clear()
        val user = fireAuth.currentUser?.uid

        user?.let { userId ->
            val pantryRef = fireBase.getReference(user).child("grocery").child("items")
            pantryRef.get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    for (childSnapshot in dataSnapshot.children) {
                        val item = childSnapshot.getValue(Item::class.java)
                        if (item != null) {
                            grocery.addItem(item.getName(), item.getQuantity())
                        }
                    }
                    displayList()
                }
            }
        }
    }

    fun displayList() {
        adapter = CustomAdapter(grocery.getItems(), applicationContext)
        listView.adapter = adapter
    }

    companion object {
        const val MA : String = "MainActivity"
    }

    fun updateDatabase() {
        val user = fireAuth.currentUser
        user?.let {
            // User is signed in, get their UID
            val uid = it.uid

            // Write user-specific data to the database
            val userRef = fireBase.getReference(uid)
            userRef.child("grocery").setValue(grocery)
                .addOnSuccessListener {
                    Log.d("GA", "Data successfully written to the database!")
                }
                .addOnFailureListener {
                    Log.e("GA", "Error writing data to the database: ", it)
                }

            userRef.child("pantry").setValue(MainActivity.pantry)
                .addOnSuccessListener {
                    Log.d("PA", "Data successfully written to the database!")
                }
                .addOnFailureListener {
                    Log.e("PA", "Error writing data to the database: ", it)
                }
        }
    }

    fun vibrate(){
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE) )
        }else{
            @Suppress("DEPRECATION")
            vib.vibrate(300)
        }
    }

    inner class CustomAdapter(private val dataSet: ArrayList<Item>, mContext: Context) :
        ArrayAdapter<Any?>(mContext, R.layout.item, dataSet as List<Any?>) {
        private inner class ViewHolder {
            lateinit var txtName: TextView
            lateinit var checkBox: CheckBox
        }

        override fun getCount(): Int {
            return dataSet.size
        }

        override fun getItem(position: Int): Item {
            return dataSet[position]
        }

        override fun getView(
            position: Int,
            convertView: View?,
            parent: ViewGroup
        ): View {
            var convertView = convertView
            val viewHolder: ViewHolder
            val result: View
            if (convertView == null) {
                viewHolder = ViewHolder()
                convertView =
                    LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
                viewHolder.txtName =
                    convertView.findViewById(R.id.txtName)
                viewHolder.checkBox =
                    convertView.findViewById(R.id.checkBox)
                result = convertView
                convertView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as ViewHolder
                result = convertView
            }

            val customFont = ResourcesCompat.getFont(context, R.font.librefranklin_regular)
            viewHolder.txtName.typeface = customFont

            val isDarkMode = SettingsActivity.isDarkMode

            if (isDarkMode) {
                viewHolder.txtName.setTextColor(getResources().getColor(R.color.lightyellow))
            } else {
                viewHolder.txtName.setTextColor(getResources().getColor(R.color.darkemerald))
            }

            val item: Item = getItem(position)
            viewHolder.txtName.text = item.getName() + " (" + item.getQuantity() + ")"
            viewHolder.checkBox.isChecked = item.getChecked()
            viewHolder.checkBox.setOnCheckedChangeListener{ _, isChecked ->
                item.check()
                if (item.getChecked()) {
                    Log.i("GA", "checked!")

                    if (oneChecked == false) {
                        Log.w(MA, "generate checked buttons")
                        oneChecked = true
                        //then change the layout to allow move to pantry button
                        if (R.id.add_layout != null && R.id.quan_layout != null) {
                            var child: View = layoutInflater.inflate(R.layout.checked_layout, null)

                            var sv: ScrollView = findViewById<ScrollView>(R.id.scroll)
                            var parent = sv.parent as ViewGroup

                            parent.addView(child, parent.indexOfChild(sv) + 1)

                            //add listeners to movetopantry, delete, cancel
                            var movetopantry: Button = findViewById(R.id.move_to_pantry)
                            movetopantry.setOnClickListener {
                                Log.w(MA, "Move Clicked!")
                                // for every checked item, move it to pantry
                                var pantry = MainActivity.pantry
                                var toMove = ArrayList<Item>()
                                for (i in grocery.getItems()) {
                                    if (i.getChecked()) {
                                        Log.w(MA, "Checked item: " + i.getName())
                                        toMove.add(i)
                                        pantry.addItem(i.getName(), i.getQuantity())
                                        i.check()
                                    }
                                }
                                grocery.getItems().removeAll(toMove)
                                Log.w(MA, "new pantry: " + pantry.getItems())
                                pantry.sort()
                                updateDatabase()
                                adapter.notifyDataSetChanged()

                                oneChecked = false
                                goPantry()
                            }
                            var delete: Button = findViewById(R.id.delete)
                            delete.setOnClickListener {
                                Log.w(MA, "Delete Clicked!")
                                //for every checked item, delete it
                                var toRemove = ArrayList<Item>()
                                for (i in grocery.getItems()) {
                                    if (i.getChecked()) {
                                        Log.w(MA, "Checked item: " + i.getName())
                                        toRemove.add(i)
                                        i.check()
                                    }
                                }
                                grocery.getItems().removeAll(toRemove)
                                updateDatabase()
                                adapter.notifyDataSetChanged()

                                oneChecked = false
                                startActivity(getIntent())
                            }
                            var cancel: Button = findViewById(R.id.cancel)
                            cancel.setOnClickListener {
                                Log.w(MA, "Cancel Clicked!")

                                oneChecked = false
                                startActivity(getIntent())
                            }
                        }
                    }
                } else {
                    Log.i("GA", "unchecked!")
                }
            }
            return result
        }
    }
}