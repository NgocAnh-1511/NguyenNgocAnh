package com.example.coffeeshop.Domain

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class OrderModel(
    var orderId: String = "",
    var items: MutableList<CartModel> = mutableListOf(),
    var totalPrice: Double = 0.0,
    var orderDate: Long = System.currentTimeMillis(),
    var status: String = "Pending", // Pending, Processing, Completed, Cancelled
    var deliveryAddress: String = "",
    var phoneNumber: String = "",
    var customerName: String = "",
    var paymentMethod: String = "Tiền mặt"
) : Serializable {
    
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(orderDate))
    }
    
    fun getStatusText(): String {
        return when (status) {
            "Pending" -> "Đang chờ xử lý"
            "Processing" -> "Đang xử lý"
            "Completed" -> "Hoàn thành"
            "Cancelled" -> "Đã hủy"
            else -> status
        }
    }
    
    fun getItemCount(): Int {
        return items.sumOf { it.quantity }
    }
}

