package com.example.coffeeshop.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import com.example.coffeeshop.Adapter.AdminVoucherAdapter
import com.example.coffeeshop.Domain.VoucherModel
import com.example.coffeeshop.Manager.VoucherManager
import com.example.coffeeshop.R
import com.example.coffeeshop.Utils.dpToPx
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AdminVoucherActivity : AppCompatActivity() {
    private lateinit var binding: com.example.coffeeshop.databinding.ActivityAdminVoucherBinding
    private lateinit var voucherManager: VoucherManager
    private lateinit var voucherAdapter: AdminVoucherAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = com.example.coffeeshop.databinding.ActivityAdminVoucherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle edge-to-edge insets for header
        ViewCompat.setOnApplyWindowInsetsListener(binding.headerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParams = v.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = systemBars.top + 8.dpToPx()
            v.layoutParams = layoutParams
            insets
        }

        voucherManager = VoucherManager(this)

        setupRecyclerView()
        setupClickListeners()
        loadVouchers()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewVouchers.layoutManager = LinearLayoutManager(this)
        voucherAdapter = AdminVoucherAdapter(
            mutableListOf(),
            onEditClick = { voucher ->
                editVoucher(voucher)
            },
            onDeleteClick = { voucher ->
                showDeleteVoucherDialog(voucher)
            },
            onToggleActiveClick = { voucher ->
                toggleVoucherActive(voucher)
            }
        )
        binding.recyclerViewVouchers.adapter = voucherAdapter
    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.addVoucherBtn.setOnClickListener {
            addNewVoucher()
        }
    }

    private fun loadVouchers() {
        lifecycleScope.launch {
            val vouchers = voucherManager.getAllVouchers()
            
            if (vouchers.isEmpty()) {
                binding.recyclerViewVouchers.visibility = View.GONE
                binding.emptyVoucherTxt.visibility = View.VISIBLE
                binding.emptyVoucherTxt.alpha = 0f
                binding.emptyVoucherTxt.animate().alpha(1f).setDuration(300).start()
            } else {
                binding.recyclerViewVouchers.visibility = View.VISIBLE
                binding.emptyVoucherTxt.visibility = View.GONE
                voucherAdapter.updateList(vouchers)
                binding.recyclerViewVouchers.alpha = 0f
                binding.recyclerViewVouchers.animate().alpha(1f).setDuration(300).start()
            }
        }
    }

    private fun addNewVoucher() {
        // TODO: Navigate to Add/Edit Voucher Activity or show dialog
        Toast.makeText(this, "Chức năng thêm mã giảm giá sẽ được triển khai", Toast.LENGTH_SHORT).show()
    }

    private fun editVoucher(voucher: VoucherModel) {
        // TODO: Navigate to Add/Edit Voucher Activity or show dialog
        Toast.makeText(this, "Chức năng chỉnh sửa mã giảm giá sẽ được triển khai", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteVoucherDialog(voucher: VoucherModel) {
        AlertDialog.Builder(this)
            .setTitle("Xóa mã giảm giá")
            .setMessage("Bạn có chắc chắn muốn xóa mã giảm giá \"${voucher.code}\"?")
            .setPositiveButton("Xóa") { _, _ ->
                lifecycleScope.launch {
                    val success = voucherManager.deleteVoucher(voucher.voucherId)
                    if (success) {
                        Toast.makeText(this@AdminVoucherActivity, "Đã xóa mã giảm giá", Toast.LENGTH_SHORT).show()
                        loadVouchers()
                    } else {
                        Toast.makeText(this@AdminVoucherActivity, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun toggleVoucherActive(voucher: VoucherModel) {
        val newStatus = !voucher.isActive
        val updatedVoucher = voucher.copy(isActive = newStatus)
        
        lifecycleScope.launch {
            val success = voucherManager.updateVoucher(updatedVoucher)
            if (success) {
                val message = if (newStatus) "Đã kích hoạt mã giảm giá" else "Đã vô hiệu hóa mã giảm giá"
                Toast.makeText(this@AdminVoucherActivity, message, Toast.LENGTH_SHORT).show()
                loadVouchers()
            } else {
                Toast.makeText(this@AdminVoucherActivity, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadVouchers()
    }
}

