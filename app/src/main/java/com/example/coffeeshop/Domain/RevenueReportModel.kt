package com.example.coffeeshop.Domain

import com.example.coffeeshop.Utils.formatVND
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RevenueReportModel(
    var date: Long = 0L, // Timestamp của ngày
    var dateLabel: String = "", // Label hiển thị (VD: "01/12/2024", "Tháng 12/2024")
    var totalRevenue: Double = 0.0,
    var orderCount: Int = 0,
    var reportType: ReportType = ReportType.DAILY, // DAILY, MONTHLY, YEARLY
    var orders: MutableList<OrderModel> = mutableListOf() // Danh sách đơn hàng trong ngày/tháng/năm
) : Serializable {
    
    enum class ReportType {
        DAILY,   // Theo ngày
        MONTHLY, // Theo tháng
        YEARLY   // Theo năm
    }
    
    fun getFormattedRevenue(): String {
        return formatVND(totalRevenue)
    }
    
    fun getFormattedDate(): String {
        return when (reportType) {
            ReportType.DAILY -> {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.format(Date(date))
            }
            ReportType.MONTHLY -> {
                val sdf = SimpleDateFormat("MM/yyyy", Locale.getDefault())
                sdf.format(Date(date))
            }
            ReportType.YEARLY -> {
                val sdf = SimpleDateFormat("yyyy", Locale.getDefault())
                sdf.format(Date(date))
            }
        }
    }
}
