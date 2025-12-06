package com.example.coffeeshop.Domain

import java.io.Serializable

data class CartModel(
    var item: ItemsModel = ItemsModel(),
    var quantity: Int = 1
) : Serializable {
    fun getTotalPrice(): Double {
        return item.price * quantity
    }
}

