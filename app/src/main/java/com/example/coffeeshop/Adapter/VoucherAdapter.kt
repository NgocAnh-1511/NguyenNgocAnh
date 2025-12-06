package com.example.coffeeshop.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.Domain.VoucherModel
import com.example.coffeeshop.Utils.formatVND
import com.example.coffeeshop.databinding.ViewholderVoucherBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VoucherAdapter(
    private val items: MutableList<VoucherModel>,
    private val onVoucherClick: (VoucherModel) -> Unit
) : RecyclerView.Adapter<VoucherAdapter.ViewHolder>() {

    lateinit var context: Context

    class ViewHolder(val binding: ViewholderVoucherBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ViewholderVoucherBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val voucher = items[position]
        val currentTime = System.currentTimeMillis()
        val isValid = voucher.isValid(currentTime)

        // Set voucher type
        holder.binding.voucherTypeTxt.text = when {
            voucher.code.contains("NEW MEMBER", ignoreCase = true) -> "ƯU ĐÃI NEW MEMBER"
            voucher.code.contains("KAT", ignoreCase = true) -> "Kưng ƯU ĐÃI"
            else -> "ƯU ĐÃI"
        }

        // Set discount description
        val discountText = when (voucher.discountType) {
            VoucherModel.DiscountType.PERCENT -> {
                if (voucher.minOrderAmount > 0) {
                    "ƯU ĐÃI ${voucher.discountPercent.toInt()}% CHO HÓA ĐƠN TỪ ${formatVND(voucher.minOrderAmount).replace(" đ", "")}"
                } else {
                    "ƯU ĐÃI ${voucher.discountPercent.toInt()}%"
                }
            }
            VoucherModel.DiscountType.AMOUNT -> {
                if (voucher.minOrderAmount > 0) {
                    "ƯU ĐÃI ${formatVND(voucher.discountAmount).replace(" đ", "")} CHO HÓA ĐƠN TỪ ${formatVND(voucher.minOrderAmount).replace(" đ", "")}"
                } else {
                    "ƯU ĐÃI ${formatVND(voucher.discountAmount).replace(" đ", "")}"
                }
            }
        }
        holder.binding.voucherDescriptionTxt.text = discountText

        // Set usage limit
        if (voucher.usageLimit > 0) {
            val remaining = voucher.usageLimit - voucher.usedCount
            holder.binding.usageLimitTxt.text = "$remaining lượt sử dụng còn lại"
            holder.binding.usageLimitTxt.visibility = View.VISIBLE
        } else {
            holder.binding.usageLimitTxt.visibility = View.GONE
        }

        // Set expiry date
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val expiryDate = dateFormat.format(Date(voucher.endDate))
        holder.binding.expiryDateTxt.text = "HSD: $expiryDate"

        // Check if expiring soon (within 7 days)
        val daysUntilExpiry = (voucher.endDate - currentTime) / (1000 * 60 * 60 * 24)
        if (daysUntilExpiry <= 7 && daysUntilExpiry > 0) {
            holder.binding.expiringSoonBanner.visibility = View.VISIBLE
        } else {
            holder.binding.expiringSoonBanner.visibility = View.GONE
        }

        // Set description if available
        if (voucher.description.isNotEmpty()) {
            holder.binding.voucherDetailsTxt.text = voucher.description
            holder.binding.voucherDetailsTxt.visibility = View.VISIBLE
        } else {
            holder.binding.voucherDetailsTxt.visibility = View.GONE
        }

        // Set button state
        if (isValid) {
            holder.binding.selectBtn.text = "Chọn"
            holder.binding.selectBtn.isEnabled = true
            holder.binding.selectBtn.alpha = 1.0f
        } else {
            holder.binding.selectBtn.text = "Áp dụng"
            holder.binding.selectBtn.isEnabled = false
            holder.binding.selectBtn.alpha = 0.5f
        }

        // Click listener
        holder.binding.selectBtn.setOnClickListener {
            if (isValid) {
                onVoucherClick(voucher)
            }
        }

        holder.binding.root.setOnClickListener {
            if (isValid) {
                onVoucherClick(voucher)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: MutableList<VoucherModel>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}

