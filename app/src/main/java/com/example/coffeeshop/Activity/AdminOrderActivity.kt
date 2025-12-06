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
import com.example.coffeeshop.Adapter.AdminOrderAdapter
import com.example.coffeeshop.Domain.OrderModel
import com.example.coffeeshop.Manager.OrderManager
import com.example.coffeeshop.R
import com.example.coffeeshop.Utils.dpToPx
import com.example.coffeeshop.databinding.ActivityAdminOrderBinding
import com.google.gson.Gson

class AdminOrderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminOrderBinding
    private lateinit var orderManager: OrderManager
    private lateinit var orderAdapter: AdminOrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdminOrderBinding.inflate(layoutInflater)
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

        setupRecyclerView()
        setupClickListeners()
        loadOrders()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewOrders.layoutManager = LinearLayoutManager(this)
        orderAdapter = AdminOrderAdapter(
            mutableListOf(),
            onViewDetailClick = { order ->
                viewOrderDetail(order)
            },
            onApproveClick = { order ->
                showApproveOrderDialog(order)
            },
            onRejectClick = { order ->
                showRejectOrderDialog(order)
            }
        )
        binding.recyclerViewOrders.adapter = orderAdapter
    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun loadOrders() {
        lifecycleScope.launch {
            val orders = orderManager.getAllOrdersForAdmin()
            
            if (orders.isEmpty()) {
                binding.recyclerViewOrders.visibility = View.GONE
                binding.emptyOrderTxt.visibility = View.VISIBLE
                binding.emptyOrderTxt.alpha = 0f
                binding.emptyOrderTxt.animate().alpha(1f).setDuration(300).start()
            } else {
                binding.recyclerViewOrders.visibility = View.VISIBLE
                binding.emptyOrderTxt.visibility = View.GONE
                orderAdapter.updateList(orders)
                binding.recyclerViewOrders.alpha = 0f
                binding.recyclerViewOrders.animate().alpha(1f).setDuration(300).start()
            }
        }
    }

    private fun viewOrderDetail(order: OrderModel) {
        val intent = Intent(this, OrderDetailActivity::class.java)
        val orderJson = Gson().toJson(order)
        intent.putExtra("order", orderJson)
        startActivity(intent)
    }

    private fun showApproveOrderDialog(order: OrderModel) {
        AlertDialog.Builder(this)
            .setTitle("Duyệt đơn hàng")
            .setMessage("Bạn có chắc chắn muốn duyệt đơn hàng này?")
            .setPositiveButton("Duyệt") { _, _ ->
                lifecycleScope.launch {
                    val success = orderManager.updateOrderStatus(order.orderId, "Completed")
                    if (success) {
                        Toast.makeText(this@AdminOrderActivity, "Đã duyệt đơn hàng", Toast.LENGTH_SHORT).show()
                        loadOrders()
                    } else {
                        Toast.makeText(this@AdminOrderActivity, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showRejectOrderDialog(order: OrderModel) {
        AlertDialog.Builder(this)
            .setTitle("Từ chối đơn hàng")
            .setMessage("Bạn có chắc chắn muốn từ chối đơn hàng này?")
            .setPositiveButton("Từ chối") { _, _ ->
                lifecycleScope.launch {
                    val success = orderManager.updateOrderStatus(order.orderId, "Cancelled")
                    if (success) {
                        Toast.makeText(this@AdminOrderActivity, "Đã từ chối đơn hàng", Toast.LENGTH_SHORT).show()
                        loadOrders()
                    } else {
                        Toast.makeText(this@AdminOrderActivity, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadOrders()
    }
}

