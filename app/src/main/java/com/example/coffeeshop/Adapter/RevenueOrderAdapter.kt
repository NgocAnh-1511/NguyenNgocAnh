package com.example.coffeeshop.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.Domain.OrderModel
import com.example.coffeeshop.Utils.formatVND
import com.example.coffeeshop.databinding.ViewholderRevenueOrderBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RevenueOrderAdapter(
    private val items: MutableList<OrderModel>
) : RecyclerView.Adapter<RevenueOrderAdapter.ViewHolder>() {

    lateinit var context: Context

    class ViewHolder(val binding: ViewholderRevenueOrderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ViewholderRevenueOrderBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = items[position]
        
        // Format order number với số thứ tự (001, 002, 003...)
        val orderNumber = (position + 1).toString().padStart(3, '0')
        holder.binding.orderNumberTxt.text = "Order #$orderNumber"
        
        // Lấy tên sản phẩm - hiển thị tất cả sản phẩm hoặc tóm tắt
        val itemNames = if (order.items.isNotEmpty()) {
            if (order.items.size == 1) {
                order.items[0].item.title
            } else {
                // Hiển thị sản phẩm đầu tiên và số lượng sản phẩm còn lại
                val firstItem = order.items[0].item.title
                val remainingCount = order.items.size - 1
                if (remainingCount == 1) {
                    "$firstItem và ${order.items[1].item.title}"
                } else {
                    "$firstItem và $remainingCount sản phẩm khác"
                }
            }
        } else {
            "Không có sản phẩm"
        }
        holder.binding.itemNameTxt.text = itemNames
        
        // Format giá tiền
        holder.binding.priceTxt.text = formatVND(order.totalPrice)
        
        // Format thời gian (hh:mm a - ví dụ: 10:00 AM)
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val timeStr = timeFormat.format(Date(order.orderDate))
        holder.binding.timeTxt.text = timeStr
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: MutableList<OrderModel>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}

