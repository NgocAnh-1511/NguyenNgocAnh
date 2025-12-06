package com.example.coffeeshop.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.Domain.OrderModel
import com.example.coffeeshop.R
import com.example.coffeeshop.databinding.ViewholderOrderBinding
import com.example.coffeeshop.Utils.formatVND
import java.util.Locale

class OrderAdapter(
    private val items: MutableList<OrderModel>,
    private val onViewDetailClick: (OrderModel) -> Unit,
    private val onCancelOrderClick: (OrderModel) -> Unit
) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    lateinit var context: Context

    class ViewHolder(val binding: ViewholderOrderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ViewholderOrderBinding.inflate(
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
        
        // Set status
        holder.binding.statusTxt.text = order.getStatusText()
        when (order.status) {
            "Pending" -> {
                holder.binding.statusTxt.setTextColor(context.getColor(R.color.orange))
                holder.binding.cancelOrderBtn.visibility = View.VISIBLE
            }
            "Processing" -> {
                holder.binding.statusTxt.setTextColor(context.getColor(R.color.orange))
                holder.binding.cancelOrderBtn.visibility = View.GONE
            }
            "Completed" -> {
                holder.binding.statusTxt.setTextColor(context.getColor(R.color.brown))
                holder.binding.cancelOrderBtn.visibility = View.GONE
            }
            "Cancelled" -> {
                holder.binding.statusTxt.setTextColor(context.getColor(R.color.grey))
                holder.binding.cancelOrderBtn.visibility = View.GONE
            }
        }

        // View detail button
        holder.binding.viewDetailBtn.setOnClickListener {
            onViewDetailClick(order)
        }

        // Cancel order button
        holder.binding.cancelOrderBtn.setOnClickListener {
            onCancelOrderClick(order)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: MutableList<OrderModel>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}

