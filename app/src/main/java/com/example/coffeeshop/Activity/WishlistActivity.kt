package com.example.coffeeshop.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.coffeeshop.Adapter.PopularAdapter
import com.example.coffeeshop.Domain.ItemsModel
import com.example.coffeeshop.Manager.CartManager
import com.example.coffeeshop.Manager.WishlistManager
import com.example.coffeeshop.R
import com.example.coffeeshop.Utils.dpToPx
import com.example.coffeeshop.databinding.ActivityWishlistBinding
import com.google.gson.Gson

class WishlistActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWishlistBinding
    private lateinit var wishlistManager: WishlistManager
    private lateinit var cartManager: CartManager
    private lateinit var wishlistAdapter: PopularAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWishlistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle edge-to-edge insets for header
        ViewCompat.setOnApplyWindowInsetsListener(binding.headerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParams = v.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = systemBars.top + 8.dpToPx() // Đưa header xuống một chút
            v.layoutParams = layoutParams
            insets
        }

        wishlistManager = WishlistManager(this)
        cartManager = CartManager(this)

        setupRecyclerView()
        setupClickListeners()
        loadWishlist()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewWishlist.layoutManager = GridLayoutManager(this, 2)
        wishlistAdapter = PopularAdapter(
            mutableListOf(),
            onAddToCartClick = { item ->
                if (cartManager.addToCart(item)) {
                    Toast.makeText(this, "${item.title} đã được thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                }
            }
        )
        binding.recyclerViewWishlist.adapter = wishlistAdapter
    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun loadWishlist() {
        val wishlist = wishlistManager.getWishlist()
        
        if (wishlist.isEmpty()) {
            binding.recyclerViewWishlist.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
        } else {
            binding.recyclerViewWishlist.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
            wishlistAdapter.updateList(wishlist)
        }
    }

    override fun onResume() {
        super.onResume()
        loadWishlist()
    }
}

