package com.example.coffeeshop.Domain

import java.io.Serializable

data class VoucherModel(
    var voucherId: String = "",
    var code: String = "", // Mã giảm giá (VD: "WELCOME10")
    var discountPercent: Double = 0.0, // Phần trăm giảm giá (VD: 10.0 = 10%)
    var discountAmount: Double = 0.0, // Số tiền giảm cố định (VD: 50000 = 50.000 đ)
    var discountType: DiscountType = DiscountType.PERCENT, // PERCENT hoặc AMOUNT
    var minOrderAmount: Double = 0.0, // Đơn hàng tối thiểu để áp dụng
    var maxDiscountAmount: Double = 0.0, // Giảm giá tối đa (chỉ áp dụng với PERCENT)
    var startDate: Long = 0L, // Ngày bắt đầu
    var endDate: Long = 0L, // Ngày kết thúc
    var usageLimit: Int = 0, // Số lần sử dụng tối đa (0 = không giới hạn)
    var usedCount: Int = 0, // Số lần đã sử dụng
    var isActive: Boolean = true, // Trạng thái hoạt động
    var description: String = "" // Mô tả mã giảm giá
) : Serializable {
    
    enum class DiscountType {
        PERCENT, // Giảm theo phần trăm
        AMOUNT   // Giảm theo số tiền cố định
    }
    
    /**
     * Kiểm tra mã giảm giá có hợp lệ không
     */
    fun isValid(currentTime: Long = System.currentTimeMillis()): Boolean {
        if (!isActive) return false
        if (currentTime < startDate || currentTime > endDate) return false
        if (usageLimit > 0 && usedCount >= usageLimit) return false
        return true
    }
    
    /**
     * Tính số tiền giảm giá
     */
    fun calculateDiscount(orderAmount: Double): Double {
        if (!isValid()) return 0.0
        if (orderAmount < minOrderAmount) return 0.0
        
        val discount = when (discountType) {
            DiscountType.PERCENT -> {
                val percentDiscount = orderAmount * discountPercent / 100
                if (maxDiscountAmount > 0 && percentDiscount > maxDiscountAmount) {
                    maxDiscountAmount
                } else {
                    percentDiscount
                }
            }
            DiscountType.AMOUNT -> {
                if (discountAmount > orderAmount) orderAmount else discountAmount
            }
        }
        
        return discount
    }
    
    /**
     * Lấy mô tả mã giảm giá
     */
    fun getDescriptionText(): String {
        return when (discountType) {
            DiscountType.PERCENT -> {
                if (maxDiscountAmount > 0) {
                    "Giảm ${discountPercent.toInt()}% tối đa ${maxDiscountAmount.toInt()} đ"
                } else {
                    "Giảm ${discountPercent.toInt()}%"
                }
            }
            DiscountType.AMOUNT -> {
                "Giảm ${discountAmount.toInt()} đ"
            }
        }
    }
}

