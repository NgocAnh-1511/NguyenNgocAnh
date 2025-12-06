package com.example.coffeeshop.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.Domain.OrderModel
import com.example.coffeeshop.R
import com.example.coffeeshop.databinding.ViewholderAdminOrderBinding
import com.example.coffeeshop.Utils.formatVND
import java.util.Locale

class AdminOrderAdapter(
    private val items: MutableList<OrderModel>,
    private val onViewDetailClick: (OrderModel) -> Unit,
    private val onApproveClick: (OrderModel) -> Unit,
    private val onRejectClick: (OrderModel) -> Unit
) : RecyclerView.Adapter<AdminOrderAdapter.ViewHolder>() {

    lateinit var context: Context

    class ViewHolder(val binding: ViewholderAdminOrderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ViewholderAdminOrderBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = items[position]

        holder.binding.orderIdTxt.text = "Order #${order.orderId.take(8)}"
        holder.binding.orderDateTxt.text = order.getFormattedDate()
        holder.binding.itemCountTxt.text = order.getItemCount().toString()
        holder.binding.totalPriceTxt.text = formatVND(order.totalPrice)
        
        // Customer name
        holder.binding.customerNameTxt.text = "Khách hàng: ${if (order.customerName.isNotEmpty()) order.customerName else order.phoneNumber}"
        
        // Delivery address
        if (order.deliveryAddress.isNotEmpty()) {
            holder.binding.deliveryAddressTxt.text = "Địa chỉ: ${order.deliveryAddress}"
            holder.binding.deliveryAddressTxt.visibility = View.VISIBLE
        } else {
            holder.binding.deliveryAddressTxt.visibility = View.GONE
        }
        
        // Set status
        holder.binding.statusTxt.text = order.getStatusText()
        when (order.status) {
            "Pending" -> {
                holder.binding.statusTxt.setTextColor(context.getColor(R.color.orange))
                holder.binding.approveOrderBtn.visibility = View.VISIBLE
                holder.binding.rejectOrderBtn.visibility = View.VISIBLE
            }
            "Processing" -> {
                holder.binding.statusTxt.setTextColor(context.getColor(R.color.orange))
                holder.binding.approveOrderBtn.visibility = View.GONE
                holder.binding.rejectOrderBtn.visibility = View.GONE
            }
            "Completed" -> {
                holder.binding.statusTxt.setTextColor(context.getColor(R.color.brown))
                holder.binding.approveOrderBtn.visibility = View.GONE
                holder.binding.rejectOrderBtn.visibility = View.GONE
            }
            "Cancelled" -> {
                holder.binding.statusTxt.setTextColor(context.getColor(R.color.grey))
                holder.binding.approveOrderBtn.visibility = View.GONE
                holder.binding.rejectOrderBtn.visibility = View.GONE
            }
        }

        // View detail button
        holder.binding.viewDetailBtn.setOnClickListener {
            onViewDetailClick(order)
        }

        // Approve order button
        holder.binding.approveOrderBtn.setOnClickListener {
            onApproveClick(order)
        }

        // Reject order button
        holder.binding.rejectOrderBtn.setOnClickListener {
            onRejectClick(order)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: MutableList<OrderModel>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}

