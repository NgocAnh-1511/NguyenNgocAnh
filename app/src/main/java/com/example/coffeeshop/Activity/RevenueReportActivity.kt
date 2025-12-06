package com.example.coffeeshop.Activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import com.example.coffeeshop.Adapter.RevenueReportAdapter
import com.example.coffeeshop.Domain.OrderModel
import com.example.coffeeshop.Domain.RevenueReportModel
import com.example.coffeeshop.Manager.FirebaseSyncManager
import com.example.coffeeshop.Manager.OrderManager
import com.example.coffeeshop.Manager.UserManager
import com.example.coffeeshop.R
import com.example.coffeeshop.Utils.dpToPx
import com.example.coffeeshop.Utils.formatVND
import com.example.coffeeshop.databinding.ActivityRevenueReportBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RevenueReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRevenueReportBinding
    private lateinit var orderManager: OrderManager
    private lateinit var reportAdapter: RevenueReportAdapter
    private lateinit var userManager: UserManager
    private lateinit var syncManager: FirebaseSyncManager
    private var currentReportType = RevenueReportModel.ReportType.DAILY
    private val reports = mutableListOf<RevenueReportModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRevenueReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle edge-to-edge insets for header
        ViewCompat.setOnApplyWindowInsetsListener(binding.headerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParams = v.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = systemBars.top + 8.dpToPx()
            v.layoutParams = layoutParams
            insets
        }

        orderManager = OrderManager(this)
        userManager = UserManager(this)
        syncManager = FirebaseSyncManager(this)

        setupRecyclerView()
        setupClickListeners()
        loadRevenueReport(RevenueReportModel.ReportType.DAILY)
    }

    private fun setupRecyclerView() {
        android.util.Log.d("RevenueReport", "setupRecyclerView called")
        binding.recyclerViewReport.layoutManager = LinearLayoutManager(this)
        reportAdapter = RevenueReportAdapter(reports)
        binding.recyclerViewReport.adapter = reportAdapter
        android.util.Log.d("RevenueReport", "RecyclerView setup complete, initial itemCount=${reportAdapter.itemCount}")
    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.dailyBtn.setOnClickListener {
            currentReportType = RevenueReportModel.ReportType.DAILY
            updateFilterButtons()
            loadRevenueReport(RevenueReportModel.ReportType.DAILY)
        }

        binding.monthlyBtn.setOnClickListener {
            currentReportType = RevenueReportModel.ReportType.MONTHLY
            updateFilterButtons()
            loadRevenueReport(RevenueReportModel.ReportType.MONTHLY)
        }

        binding.yearlyBtn.setOnClickListener {
            currentReportType = RevenueReportModel.ReportType.YEARLY
            updateFilterButtons()
            loadRevenueReport(RevenueReportModel.ReportType.YEARLY)
        }
    }

    private fun updateFilterButtons() {
        val selectedColor = getColor(R.color.orange)
        val selectedTextColor = getColor(R.color.white)
        val unselectedColor = getColor(R.color.lightCream)
        val unselectedTextColor = getColor(R.color.darkBrown)

        when (currentReportType) {
            RevenueReportModel.ReportType.DAILY -> {
                binding.dailyBtn.setBackgroundColor(selectedColor)
                binding.dailyBtn.setTextColor(selectedTextColor)
                binding.monthlyBtn.setBackgroundColor(unselectedColor)
                binding.monthlyBtn.setTextColor(unselectedTextColor)
                binding.yearlyBtn.setBackgroundColor(unselectedColor)
                binding.yearlyBtn.setTextColor(unselectedTextColor)
            }
            RevenueReportModel.ReportType.MONTHLY -> {
                binding.dailyBtn.setBackgroundColor(unselectedColor)
                binding.dailyBtn.setTextColor(unselectedTextColor)
                binding.monthlyBtn.setBackgroundColor(selectedColor)
                binding.monthlyBtn.setTextColor(selectedTextColor)
                binding.yearlyBtn.setBackgroundColor(unselectedColor)
                binding.yearlyBtn.setTextColor(unselectedTextColor)
            }
            RevenueReportModel.ReportType.YEARLY -> {
                binding.dailyBtn.setBackgroundColor(unselectedColor)
                binding.dailyBtn.setTextColor(unselectedTextColor)
                binding.monthlyBtn.setBackgroundColor(unselectedColor)
                binding.monthlyBtn.setTextColor(unselectedTextColor)
                binding.yearlyBtn.setBackgroundColor(selectedColor)
                binding.yearlyBtn.setTextColor(selectedTextColor)
            }
        }
    }

    private fun loadRevenueReport(reportType: RevenueReportModel.ReportType) {
        // Sync all data from Firebase first to ensure we have the latest order statuses
        val currentUserId = userManager.getUserId()
        
        if (currentUserId != null) {
            android.util.Log.d("RevenueReport", "Starting Firebase sync before loading report...")
            syncManager.syncAllDataFromFirebaseAsync(currentUserId) { success ->
                if (success) {
                    android.util.Log.d("RevenueReport", "Firebase sync successful before loading report.")
                    loadRevenueReportInternal(reportType)
                } else {
                    android.util.Log.e("RevenueReport", "Firebase sync failed before loading report.")
                    Toast.makeText(this, "Không thể đồng bộ dữ liệu mới nhất từ Firebase.", Toast.LENGTH_SHORT).show()
                    loadRevenueReportInternal(reportType) // Load anyway with potentially old data
                }
            }
        } else {
            android.util.Log.w("RevenueReport", "No current user ID found, loading report without Firebase sync.")
            loadRevenueReportInternal(reportType)
        }
    }
    
    private fun loadRevenueReportInternal(reportType: RevenueReportModel.ReportType) {
        lifecycleScope.launch {
            val allOrders = orderManager.getAllOrdersForAdmin()
        
        // Debug: Log tất cả orders và status để kiểm tra
        android.util.Log.d("RevenueReport", "========================================")
        android.util.Log.d("RevenueReport", "=== LOADING REVENUE REPORT ===")
        android.util.Log.d("RevenueReport", "Report Type: $reportType")
        android.util.Log.d("RevenueReport", "Total orders in database: ${allOrders.size}")
        android.util.Log.d("RevenueReport", "========================================")
        
        // Log chi tiết từng order và tất cả các status unique
        val uniqueStatuses = mutableSetOf<String>()
        allOrders.forEach { order ->
            val statusTrimmed = order.status.trim()
            uniqueStatuses.add(statusTrimmed)
            android.util.Log.d("RevenueReport", "Order ID: ${order.orderId.take(12)}... | Status: '$statusTrimmed' | Price: $${order.totalPrice} | Date: ${order.orderDate}")
        }
        
        android.util.Log.d("RevenueReport", "--- Unique Statuses Found: $uniqueStatuses ---")
        
        // Chỉ lấy các đơn đã hoàn thành (trim và không phân biệt hoa thường)
        val completedOrders = allOrders.filter { order ->
            val statusTrimmed = order.status.trim()
            val isCompleted = statusTrimmed.equals("Completed", ignoreCase = true)
            isCompleted
        }
        
        android.util.Log.d("RevenueReport", "========================================")
        android.util.Log.d("RevenueReport", "=== FILTER RESULTS ===")
        android.util.Log.d("RevenueReport", "Total orders: ${allOrders.size}")
        android.util.Log.d("RevenueReport", "Completed orders count: ${completedOrders.size}")
        val completedRevenue = completedOrders.sumOf { it.totalPrice }
        android.util.Log.d("RevenueReport", "Total revenue from completed orders: $$completedRevenue")
        android.util.Log.d("RevenueReport", "========================================")

        if (completedOrders.isEmpty()) {
            binding.recyclerViewReport.visibility = View.GONE
            binding.emptyReportTxt.visibility = View.VISIBLE
            binding.totalRevenueTxt.text = formatVND(0.0)
            binding.totalOrdersTxt.text = "Tổng số đơn: 0"
            reports.clear()
            reportAdapter.updateList(reports)
            return@launch
        }

        binding.recyclerViewReport.visibility = View.VISIBLE
        binding.emptyReportTxt.visibility = View.GONE

        // Tính toán báo cáo theo loại
        val reportMap = mutableMapOf<Long, RevenueReportModel>()

        completedOrders.forEach { order ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = order.orderDate
            }

            val key = when (reportType) {
                RevenueReportModel.ReportType.DAILY -> {
                    // Key theo ngày (bỏ qua giờ, phút, giây)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }
                RevenueReportModel.ReportType.MONTHLY -> {
                    // Key theo tháng
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }
                RevenueReportModel.ReportType.YEARLY -> {
                    // Key theo năm
                    calendar.set(Calendar.MONTH, 0)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }
            }

            val report = reportMap.getOrPut(key) {
                RevenueReportModel(
                    date = key,
                    dateLabel = "",
                    totalRevenue = 0.0,
                    orderCount = 0,
                    reportType = reportType,
                    orders = mutableListOf()
                )
            }

            report.totalRevenue += order.totalPrice
            report.orderCount++
            report.orders.add(order) // Thêm đơn hàng vào danh sách
            android.util.Log.d("RevenueReport", "Added order ${order.orderId.take(8)} to report ${report.getFormattedDate()}. Total orders in report: ${report.orders.size}")
        }

        // Chuyển map thành list và sắp xếp theo ngày giảm dần
        val sortedReports = reportMap.values.sortedByDescending { it.date }
        
        android.util.Log.d("RevenueReport", "=== REPORT GENERATION ===")
        android.util.Log.d("RevenueReport", "Number of reports generated: ${sortedReports.size}")
        sortedReports.forEach { report ->
            android.util.Log.d("RevenueReport", "Report: date=${report.getFormattedDate()}, revenue=${report.getFormattedRevenue()}, orders=${report.orderCount}, ordersListSize=${report.orders.size}")
            report.orders.forEachIndexed { index, order ->
                android.util.Log.d("RevenueReport", "  Order[$index]: ${order.orderId.take(8)} - ${order.items.firstOrNull()?.item?.title ?: "N/A"} - ${order.totalPrice}")
            }
        }
        
        // Tính tổng TRƯỚC KHI update adapter
        val totalRevenue = sortedReports.sumOf { it.totalRevenue }
        val totalOrders = sortedReports.sumOf { it.orderCount }
        
        android.util.Log.d("RevenueReport", "=== CALCULATED TOTALS ===")
        android.util.Log.d("RevenueReport", "Total revenue: $$totalRevenue")
        android.util.Log.d("RevenueReport", "Total orders: $totalOrders")
        
        // Update reports list và adapter
        reports.clear()
        reports.addAll(sortedReports)
        android.util.Log.d("RevenueReport", "Before updateList: reports.size=${reports.size}")
        reportAdapter.updateList(reports)
        android.util.Log.d("RevenueReport", "After updateList: adapter.itemCount=${reportAdapter.itemCount}")

        android.util.Log.d("RevenueReport", "=== UI UPDATE ===")
        android.util.Log.d("RevenueReport", "Total revenue displayed: $$totalRevenue")
        android.util.Log.d("RevenueReport", "Total orders displayed: $totalOrders")
        
        binding.totalRevenueTxt.text = formatVND(totalRevenue)
        binding.totalOrdersTxt.text = "Tổng số đơn: $totalOrders"
        
        android.util.Log.d("RevenueReport", "UI text set: revenue='${binding.totalRevenueTxt.text}', orders='${binding.totalOrdersTxt.text}'")
        }
    }

    override fun onResume() {
        super.onResume()
        // Đồng bộ từ Firebase trước khi load báo cáo
        loadRevenueReport(currentReportType)
    }
}

