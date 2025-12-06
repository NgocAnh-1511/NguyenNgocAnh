package com.example.coffeeshop.Domain

import java.io.Serializable

data class AddressModel(
    var id: String = "",
    var name: String = "",
    var phone: String = "",
    var address: String = "",
    var isDefault: Boolean = false
) : Serializable

