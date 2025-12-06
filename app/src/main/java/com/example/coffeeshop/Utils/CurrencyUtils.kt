package com.example.coffeeshop.Utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Format số tiền sang định dạng VND (ví dụ: 19.300 đ hoặc 450.000 đ)
 * Làm tròn đến số nguyên gần nhất
 * Nếu giá là đô la (nhỏ hơn 1000), nhân với 1000 để chuyển sang VND
 */
fun formatVND(amount: Double): String {
    // Nếu giá nhỏ hơn 1000, có thể là đô la, nhân với 1000 để chuyển sang VND
    // Ví dụ: 19.3 đô la -> 19.300 VND
    val vndAmount = if (amount < 1000) {
        (amount * 1000).toLong()
    } else {
        amount.toLong()
    }
    val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))
    formatter.groupingSize = 3
    return "${formatter.format(vndAmount)} đ"
}

/**
 * Format số tiền sang định dạng VND với số thập phân (ví dụ: 19.300,50 đ)
 */
fun formatVNDWithDecimal(amount: Double): String {
    val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))
    formatter.groupingSize = 3
    val integerPart = formatter.format(amount.toLong())
    val decimalPart = (amount - amount.toLong()).let { 
        if (it > 0) String.format(Locale.getDefault(), "%.0f", it * 100) else "00"
    }
    return if (decimalPart != "00") "$integerPart,$decimalPart đ" else "$integerPart đ"
}

