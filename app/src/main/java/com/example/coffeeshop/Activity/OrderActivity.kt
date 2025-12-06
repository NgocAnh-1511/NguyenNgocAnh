package com.example.coffeeshop.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import com.example.coffeeshop.Adapter.OrderAdapter
import com.example.coffeeshop.Domain.OrderModel
import com.example.coffeeshop.Manager.FirebaseSyncManager
import com.example.coffeeshop.Manager.OrderManager
import com.example.coffeeshop.Manager.UserManager
import com.example.coffeeshop.R
import com.example.coffeeshop.Utils.dpToPx
import com.example.coffeeshop.databinding.ActivityOrderBinding
import com.google.gson.Gson

class OrderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderBinding
    private lateinit var orderManager: OrderManager
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var userManager: UserManager
    private lateinit var syncManager: FirebaseSyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle edge-to-edge insets for header
        ViewCompat.setOnApplyWindowInsetsListener(binding.headerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParams = v.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = systemBars.top + 8.dpToPx() // Đưa header xuống một chút
            v.layoutParams = layoutParams
            insets
        }

        orderManager = OrderManager(this)
        userManager = UserManager(this)
        syncManager = FirebaseSyncManager(this)

        setupRecyclerView()
        setupClickListeners()
        loadOrders()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewOrders.layoutManager = LinearLayoutManager(this)
        orderAdapter = OrderAdapter(
            mutableListOf(),
            onViewDetailClick = { order ->
                viewOrderDetail(order)
            },
            onCancelOrderClick = { order ->
                showCancelOrderDialog(order)
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
            val orders = orderManager.getAllOrders()
        
        if (orders.isEmpty()) {
            binding.recyclerViewOrders.visibility = View.GONE
            binding.emptyOrderTxt.visibility = View.VISIBLE
            // Add fade animation
            binding.emptyOrderTxt.alpha = 0f
            binding.emptyOrderTxt.animate().alpha(1f).setDuration(300).start()
        } else {
            binding.recyclerViewOrders.visibility = View.VISIBLE
            binding.emptyOrderTxt.visibility = View.GONE
            orderAdapter.updateList(orders)
            // Add fade animation
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

    private fun showCancelOrderDialog(order: OrderModel) {
        AlertDialog.Builder(this)
            .setTitle("Hủy đơn hàng")
            .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này?")
            .setPositiveButton("Hủy đơn") { _, _ ->
                lifecycleScope.launch {
                    val success = orderManager.cancelOrder(order.orderId)
                    if (success) {
                        Toast.makeText(this@OrderActivity, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show()
                        loadOrders()
                    } else {
                        Toast.makeText(this@OrderActivity, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Không", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Đồng bộ từ Firebase trước khi load orders để có dữ liệu mới nhất
        val currentUser = userManager.getCurrentUser()
        if (currentUser != null) {
            syncManager.syncAllDataFromFirebaseAsync(currentUser.userId) { success ->
                // Sau khi sync xong, load lại orders
                loadOrders()
            }
        } else {
            loadOrders()
        }
    }
}

