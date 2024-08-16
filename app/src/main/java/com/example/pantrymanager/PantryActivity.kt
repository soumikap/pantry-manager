package com.example.pantrymanager

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class PantryActivity : AppCompatActivity() {
    private lateinit var listView : ListView
    private lateinit var pantry : ItemList
    private lateinit var fireBase: FirebaseDatabase
    private lateinit var adView : AdView
    private lateinit var fireAuth : FirebaseAuth
    private var itemlist : ArrayList<String> = ArrayList<String>()

    private lateinit var adapter: CustomAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantry)

        pantry = MainActivity.pantry
        listView = findViewById(R.id.list)
        fireAuth = MainActivity.fireAuth
        fireBase = MainActivity.fireBase

        readDatabase()

        Log.i("PA", "post data base " + pantry.getItems().size)

        //set up the button
        var confirm : Button = findViewById(R.id.add_button)
        var handler = AddButtonHandler()
        confirm.setOnClickListener(handler)

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

        var grocery : ImageButton = findViewById(R.id.go_to_grocery)
        grocery.setOnClickListener{goGrocery()}
    }

    private fun goSettings() {
        var intent : Intent = Intent(this,SettingsActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun delete(item: String) {
        Log.w(MA, "start delete!")
        var quan : EditText = findViewById(R.id.quantity_et)
        if (quan != null && quan.text.toString() != "") {
            var num = quan.text.toString().toInt()
            Log.w(MA, "deleting: " + item + ", " + num)
            pantry.subItem(item, num)
            updateDatabase()
            startActivity(getIntent())
        }
    }

    inner class ListItemHandler : AdapterView.OnItemLongClickListener {
        override fun onItemLongClick(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ): Boolean {

            var selectedItem : String = itemlist.get( position )
            Log.i("PA", "selected item " + selectedItem)
            var item : String = selectedItem.substring(0, selectedItem.indexOf('(') )
            Log.i("PA", "edited item " + item)
            setContentView(R.layout.pantry_delete)
            findViewById<TextView>(R.id.to_delete).text = selectedItem
            var deleteB : Button = findViewById(R.id.delete_button)
            deleteB.setOnClickListener{delete(item)}

            var cancelB : Button = findViewById(R.id.cancel_button)
            cancelB.setOnClickListener{
                finish()
                startActivity(intent)
            }

            return true
        }
    }

    private fun goGrocery() {
        finish()
        var intent : Intent = Intent(this,GroceryActivity::class.java)
        startActivity(intent)
    }

    fun readDatabase() {
        val user = fireAuth.currentUser?.uid
        pantry.clear()
        user?.let { _ ->
            val pantryRef = fireBase.getReference(user).child("pantry").child("items")
            pantryRef.get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    for (childSnapshot in dataSnapshot.children) {
                        val item = childSnapshot.getValue(Item::class.java)
                        if (item != null) {
                            pantry.addItem(item.getName(), item.getQuantity())
                        }
                    }
                    displayList()
                }
            }
        }
    }

    fun updateDatabase() {
        val user = fireAuth.currentUser
        user?.let {
            val uid = it.uid

            val userRef = fireBase.getReference(uid)
            userRef.child("pantry").setValue(pantry)
                .addOnSuccessListener {
                    Log.d("PA", "Data successfully written to the database!")
                }
                .addOnFailureListener {
                    Log.e("PA", "Error writing data to the database: ", it)
                }
        }
    }


    fun displayList() {
        listView = findViewById(R.id.list)
        // create a list of "item (quantity)"
        var list : ArrayList<String> = ArrayList<String>()
        for( entry in pantry.getItems() ) {
            list.add(entry.getName() + " (" + entry.getQuantity() + ")")
        }
        list.sort()
        itemlist.clear()
        itemlist.addAll(list)
        Log.w(MA, "list: \n" + list)

        // tie adapter to listView
        adapter = CustomAdapter(pantry.getItems(), applicationContext)
        listView.adapter = adapter

        // set up event handling
        var lih : ListItemHandler = ListItemHandler()
        listView.onItemLongClickListener = lih

    }

    inner class AddButtonHandler : OnClickListener {
        override fun onClick(v: View?) {
            Log.w(MA, "clicked!")
            var item = findViewById<EditText>(R.id.add_et)
            var q = findViewById<EditText>(R.id.quantity_et)
            if (item != null && q != null && item.text.toString() != "" && q.text.toString() != "") {
                //add the item into the pantry and display list again
                var i : String = item.text.toString()
                var qu : Int = q.text.toString().toInt()
                vibrate()
                pantry.addItem(i, qu)
                pantry.sort()
                displayList()
                updateDatabase()
                item.setText("")
                q.setText("")
            }
        }
    }

    companion object {
        const val MA : String = "MainActivity"
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
                    LayoutInflater.from(parent.context).inflate(R.layout.item_pantry, parent, false)
                viewHolder.txtName =
                    convertView.findViewById(R.id.txtName)
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

            return result
        }
    }

}