package com.example.coffeeshop.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.Domain.VoucherModel
import com.example.coffeeshop.R
import com.example.coffeeshop.Utils.formatVND
import com.example.coffeeshop.databinding.ViewholderAdminVoucherBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminVoucherAdapter(
    private val items: MutableList<VoucherModel>,
    private val onEditClick: (VoucherModel) -> Unit,
    private val onDeleteClick: (VoucherModel) -> Unit,
    private val onToggleActiveClick: (VoucherModel) -> Unit
) : RecyclerView.Adapter<AdminVoucherAdapter.ViewHolder>() {

    lateinit var context: Context

    class ViewHolder(val binding: ViewholderAdminVoucherBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ViewholderAdminVoucherBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val voucher = items[position]

        holder.binding.voucherCodeTxt.text = voucher.code
        holder.binding.voucherDescriptionTxt.text = voucher.getDescriptionText()
        
        // Format dates
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.binding.startDateTxt.text = "Từ: ${dateFormat.format(Date(voucher.startDate))}"
        holder.binding.endDateTxt.text = "Đến: ${dateFormat.format(Date(voucher.endDate))}"
        
        // Usage info
        val usageText = if (voucher.usageLimit == 0) {
            "Đã dùng: ${voucher.usedCount} (Không giới hạn)"
        } else {
            "Đã dùng: ${voucher.usedCount}/${voucher.usageLimit}"
        }
        holder.binding.usageTxt.text = usageText
        
        // Min order amount
        if (voucher.minOrderAmount > 0) {
            holder.binding.minOrderTxt.text = "Đơn tối thiểu: ${formatVND(voucher.minOrderAmount)}"
            holder.binding.minOrderTxt.visibility = View.VISIBLE
        } else {
            holder.binding.minOrderTxt.visibility = View.GONE
        }
        
        // Status
        if (voucher.isActive) {
            holder.binding.statusTxt.text = "Đang hoạt động"
            holder.binding.statusTxt.setTextColor(context.getColor(R.color.brown))
            holder.binding.toggleActiveBtn.text = "Vô hiệu hóa"
        } else {
            holder.binding.statusTxt.text = "Đã vô hiệu hóa"
            holder.binding.statusTxt.setTextColor(context.getColor(R.color.grey))
            holder.binding.toggleActiveBtn.text = "Kích hoạt"
        }
        
        // Check if expired
        val currentTime = System.currentTimeMillis()
        if (currentTime > voucher.endDate) {
            holder.binding.statusTxt.text = "Đã hết hạn"
            holder.binding.statusTxt.setTextColor(context.getColor(R.color.grey))
        }

        // Edit button
        holder.binding.editBtn.setOnClickListener {
            onEditClick(voucher)
        }

        // Delete button
        holder.binding.deleteBtn.setOnClickListener {
            onDeleteClick(voucher)
        }

        // Toggle active button
        holder.binding.toggleActiveBtn.setOnClickListener {
            onToggleActiveClick(voucher)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: MutableList<VoucherModel>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}

