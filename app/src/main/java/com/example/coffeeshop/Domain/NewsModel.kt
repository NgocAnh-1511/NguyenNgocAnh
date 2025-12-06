package com.example.coffeeshop.Domain

import java.io.Serializable

data class NewsModel(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var date: String = "",
    var link: String = ""
) : Serializable

