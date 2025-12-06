package com.example.coffeeshop.Domain

data class UserModel(
    var userId: String = "",
    var phoneNumber: String = "",
    var fullName: String = "",
    var email: String = "",
    var password: String = "",
    var avatarPath: String = "", // Đường dẫn đến ảnh đại diện
    var createdAt: Long = System.currentTimeMillis(),
    var isAdmin: Boolean = false // Quyền admin
)

