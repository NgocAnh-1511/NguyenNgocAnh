package com.example.coffeeshop.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.Domain.RevenueReportModel
import com.example.coffeeshop.databinding.ViewholderRevenueReportBinding

class RevenueReportAdapter(
    private val items: MutableList<RevenueReportModel>
) : RecyclerView.Adapter<RevenueReportAdapter.ViewHolder>() {

    lateinit var context: Context

    class ViewHolder(val binding: ViewholderRevenueReportBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        android.util.Log.d("RevenueReportAdapter", "onCreateViewHolder called")
        val binding = ViewholderRevenueReportBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val report = items[position]

        holder.binding.dateTxt.text = report.getFormattedDate()
        holder.binding.revenueTxt.text = report.getFormattedRevenue()
        holder.binding.orderCountTxt.text = "${report.orderCount} đơn"

        // Setup RecyclerView cho danh sách đơn hàng
        // Sắp xếp đơn hàng theo thời gian (mới nhất trước)
        val sortedOrders = report.orders.sortedByDescending { it.orderDate }
        
        android.util.Log.d("RevenueReportAdapter", "Binding report for ${report.getFormattedDate()}: ${sortedOrders.size} orders")
        sortedOrders.forEachIndexed { index, order ->
            android.util.Log.d("RevenueReportAdapter", "  Order[$index]: ${order.orderId.take(8)} - ${order.items.firstOrNull()?.item?.title ?: "N/A"}")
        }
        
        // Hiển thị RecyclerView nếu có đơn hàng
        if (sortedOrders.isNotEmpty()) {
            holder.binding.recyclerViewOrders.visibility = View.VISIBLE
            
            val orderAdapter = RevenueOrderAdapter(sortedOrders.toMutableList())
            val layoutManager = LinearLayoutManager(context)
            holder.binding.recyclerViewOrders.layoutManager = layoutManager
            holder.binding.recyclerViewOrders.setHasFixedSize(false)
            holder.binding.recyclerViewOrders.isNestedScrollingEnabled = false
            holder.binding.recyclerViewOrders.adapter = orderAdapter
            
            android.util.Log.d("RevenueReportAdapter", "RecyclerView setup complete: itemCount=${orderAdapter.itemCount}")
        } else {
            holder.binding.recyclerViewOrders.visibility = View.GONE
            android.util.Log.d("RevenueReportAdapter", "No orders to display for ${report.getFormattedDate()}")
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: MutableList<RevenueReportModel>) {
        android.util.Log.d("RevenueReportAdapter", "updateList called with ${newList.size} reports")
        newList.forEachIndexed { index, report ->
            android.util.Log.d("RevenueReportAdapter", "  Report[$index]: ${report.getFormattedDate()}, orders=${report.orders.size}")
        }
        // Tạo bản sao của list để tránh reference issue
        val newListCopy = newList.map { report ->
            // Tạo bản sao của report và orders
            RevenueReportModel(
                date = report.date,
                dateLabel = report.dateLabel,
                totalRevenue = report.totalRevenue,
                orderCount = report.orderCount,
                reportType = report.reportType,
                orders = report.orders.toMutableList() // Tạo bản sao của orders list
            )
        }.toMutableList()
        
        items.clear()
        items.addAll(newListCopy)
        notifyDataSetChanged()
        android.util.Log.d("RevenueReportAdapter", "notifyDataSetChanged called, itemCount=${items.size}")
    }
}
