package com.example.pantrymanager

import android.util.Log


/*
* In MainActivity, we can have two ItemList objects
* One is GroceryList and other is Pantry
* We can use ItemList in Recipe class representing the ingredients
* */
class ItemList {
    private var items : ArrayList<Item> = ArrayList()

    fun addItem(name : String, quantity : Int) {
        val nameAdd = name.trim().lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
        val nameNew = nameAdd.trim()
        var added = false
        for (item in items) {
            if (item.getName() == nameNew) {
                item.updateQuantity(item.getQuantity() + quantity)
                added = true
            }
        }

        if (!added) {
            items.add(Item(nameAdd, quantity))
        }
    }

    fun subItem(item : String, quan : Int) {
        Log.i("item to remove", item)
        var toRemove : Item? = null
        for (i in items) {
            Log.i("IL", "item " + i.getName())
            if (i.getName().trim().equals(item.trim())) {
                var origQuan = i.getQuantity()
                if (quan >= origQuan) {
                    toRemove = i
                } else {
                    i.updateQuantity(origQuan-quan)
                }
            }
        }


        if (toRemove != null) {
            items.remove(toRemove)
        }
    }

    fun howMany(item : Item) : Int {
        return item.getQuantity()
    }

    fun clear() {
        items = ArrayList<Item>()
    }

    fun getItems() : ArrayList<Item> {
        return items
    }

    fun sort() {
        items.sortBy {list -> list.getName()}
    }
}