package com.example.pantrymanager


class Item {
    private var name: String
    private var quantity: Int
    private var checked: Boolean = false

    constructor() {
        name = ""
        quantity = 0
    }

    constructor(name : String, quantity : Int) {
        this.name = name
        this.quantity = quantity
    }

    fun updateQuantity(quantity : Int) {
        this.quantity = quantity
    }

    fun getName() : String {
        return name
    }

    fun getQuantity() : Int {
        return quantity
    }

    fun check() {
        checked = !checked
    }

    fun getChecked() : Boolean {
        return checked
    }

}