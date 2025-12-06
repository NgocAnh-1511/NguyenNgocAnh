package com.example.coffeeshop.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshop.Adapter.CartAdapter
import com.example.coffeeshop.Manager.CartManager
import com.example.coffeeshop.Manager.UserManager
import com.example.coffeeshop.Utils.dpToPx
import com.example.coffeeshop.Utils.formatVND
import com.example.coffeeshop.databinding.ActivityCartBinding

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var cartManager: CartManager
    private lateinit var userManager: UserManager
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle edge-to-edge insets for header
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.headerLayout) { v, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            val layoutParams = v.layoutParams as android.view.ViewGroup.MarginLayoutParams
            layoutParams.topMargin = systemBars.top + 8.dpToPx() // Đưa header xuống một chút
            v.layoutParams = layoutParams
            insets
        }

        cartManager = CartManager(this)
        userManager = UserManager(this)

        setupRecyclerView()
        setupClickListeners()
        loadCartItems()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewCart.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(
            mutableListOf(),
            onQuantityChanged = { itemTitle, newQuantity ->
                if (cartManager.updateQuantity(itemTitle, newQuantity)) {
                    loadCartItems()
                } else {
                    Toast.makeText(this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                }
            },
            onRemoveClick = { itemTitle ->
                if (cartManager.removeFromCart(itemTitle)) {
                    Toast.makeText(this, "Đã xóa khỏi giỏ hàng", Toast.LENGTH_SHORT).show()
                    loadCartItems()
                } else {
                    Toast.makeText(this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                }
            }
        )
        binding.recyclerViewCart.adapter = cartAdapter
    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.checkoutBtn.setOnClickListener {
            val cartList = cartManager.getCartList()
            if (cartList.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show()
            } else {
                // Kiểm tra đăng nhập trước khi thanh toán
                if (!userManager.isLoggedIn()) {
                    Toast.makeText(this, "Vui lòng đăng nhập để thanh toán", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.putExtra("redirectToCheckout", true)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, CheckoutActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun loadCartItems() {
        val cartList = cartManager.getCartList()
        
        if (cartList.isEmpty()) {
            binding.recyclerViewCart.visibility = View.GONE
            binding.emptyCartTxt.visibility = View.VISIBLE
            binding.bottomLayout.visibility = View.GONE
        } else {
            binding.recyclerViewCart.visibility = View.VISIBLE
            binding.emptyCartTxt.visibility = View.GONE
            binding.bottomLayout.visibility = View.VISIBLE
            
            cartAdapter.updateList(cartList)
            updateTotalPrice()
        }
    }

    private fun updateTotalPrice() {
        val total = cartManager.getTotalPrice()
        binding.totalPriceTxt.text = formatVND(total)
    }

    override fun onResume() {
        super.onResume()
        loadCartItems()
    }
}

