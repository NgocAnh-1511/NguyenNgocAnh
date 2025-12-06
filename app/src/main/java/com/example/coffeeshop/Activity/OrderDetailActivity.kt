package com.example.coffeeshop.Activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import com.bumptech.glide.Glide
import com.example.coffeeshop.Domain.OrderModel
import com.example.coffeeshop.Manager.OrderManager
import com.example.coffeeshop.R
import com.example.coffeeshop.Utils.dpToPx
import com.example.coffeeshop.Utils.formatVND
import com.example.coffeeshop.databinding.ActivityOrderDetailBinding
import com.google.gson.Gson
import java.util.Locale

class OrderDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderDetailBinding
    private var currentOrder: OrderModel? = null
    private var orderId: String? = null
    private lateinit var orderManager: OrderManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
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

        // Get order from intent
        val orderJson = intent.getStringExtra("order")
        if (orderJson != null) {
            currentOrder = Gson().fromJson(orderJson, OrderModel::class.java)
            orderId = currentOrder?.orderId
            setupUI()
        } else {
            finish()
        }

        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        // Reload order từ database để cập nhật status mới nhất
        orderId?.let { id ->
            lifecycleScope.launch {
                val updatedOrder = orderManager.getOrderById(id)
                if (updatedOrder != null) {
                    currentOrder = updatedOrder
                    setupUI()
                }
            }
        }
    }

    private fun setupUI() {
        currentOrder?.let { order ->
            // Set order info
            binding.orderIdTxt.text = "Order #${order.orderId.take(8)}"
            binding.orderDateTxt.text = order.getFormattedDate()
            binding.totalPriceTxt.text = formatVND(order.totalPrice)
            
            // Set status
            binding.statusTxt.text = order.getStatusText()
            when (order.status) {
                "Pending" -> binding.statusTxt.setTextColor(getColor(R.color.orange))
                "Processing" -> binding.statusTxt.setTextColor(getColor(R.color.orange))
                "Completed" -> binding.statusTxt.setTextColor(getColor(R.color.brown))
                "Cancelled" -> binding.statusTxt.setTextColor(getColor(R.color.grey))
            }

            // Set delivery info
            binding.deliveryAddressTxt.text = "Địa chỉ: ${if (order.deliveryAddress.isNotEmpty()) order.deliveryAddress else "Chưa có"}"
            binding.phoneNumberTxt.text = "Số điện thoại: ${if (order.phoneNumber.isNotEmpty()) order.phoneNumber else "Chưa có"}"
            binding.customerNameTxt.text = "Tên khách hàng: ${if (order.customerName.isNotEmpty()) order.customerName else "Chưa có"}"
            binding.paymentMethodTxt.text = "Phương thức thanh toán: ${order.paymentMethod}"

            // Setup recycler view for order items
            binding.recyclerViewOrderItems.layoutManager = LinearLayoutManager(this)
            // Create a custom adapter for read-only display
            val adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
                    val binding = com.example.coffeeshop.databinding.ViewholderCartBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                    // Hide action buttons for read-only mode
                    binding.plusBtn.visibility = View.GONE
                    binding.minusBtn.visibility = View.GONE
                    binding.removeBtn.visibility = View.GONE
                    return object : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {}
                }

                override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
                    val cartItem = order.items[position]
                    val item = cartItem.item
                    val binding = com.example.coffeeshop.databinding.ViewholderCartBinding.bind(holder.itemView)
                    
                    binding.itemTitle.text = item.title
                    val totalPrice = cartItem.getTotalPrice()
                    binding.itemPrice.text = formatVND(totalPrice)
                    binding.quantityTxt.text = cartItem.quantity.toString()

                    if (item.picUrl.isNotEmpty()) {
                        Glide.with(this@OrderDetailActivity)
                            .load(item.picUrl[0])
                            .into(binding.itemPic)
                    }
                }

                override fun getItemCount() = order.items.size
            }
            binding.recyclerViewOrderItems.adapter = adapter
        }
    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }
        
        binding.refreshStatusBtn.setOnClickListener {
            reloadOrder()
        }
    }
    
    private fun reloadOrder() {
        orderId?.let { id ->
            lifecycleScope.launch {
                val updatedOrder = orderManager.getOrderById(id)
                if (updatedOrder != null) {
                    currentOrder = updatedOrder
                    setupUI()
                    android.widget.Toast.makeText(this@OrderDetailActivity, "Đã cập nhật trạng thái", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(this@OrderDetailActivity, "Không tìm thấy đơn hàng", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

