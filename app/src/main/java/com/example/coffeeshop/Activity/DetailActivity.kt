package com.example.coffeeshop.Activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.coffeeshop.Domain.ItemsModel
import com.example.coffeeshop.Manager.CartManager
import com.example.coffeeshop.Manager.WishlistManager
import com.example.coffeeshop.R
import com.example.coffeeshop.databinding.ActivityDetailBinding
import com.google.gson.Gson
import java.util.Locale
import android.view.ViewGroup
import com.example.coffeeshop.Utils.dpToPx
import com.example.coffeeshop.Utils.formatVND

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var cartManager: CartManager
    private lateinit var wishlistManager: WishlistManager
    private var currentItem: ItemsModel? = null
    private var selectedSize: String = "Medium" // Default size
    private var quantity: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle edge-to-edge insets properly for responsive design
        ViewCompat.setOnApplyWindowInsetsListener(binding.headerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParams = v.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = systemBars.top + 8.dpToPx() // Đưa header xuống một chút
            v.layoutParams = layoutParams
            insets
        }

        cartManager = CartManager(this)
        wishlistManager = WishlistManager(this)

        // Get item from intent
        val itemJson = intent.getStringExtra("item")
        if (itemJson != null) {
            currentItem = Gson().fromJson(itemJson, ItemsModel::class.java)
            setupUI()
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupClickListeners()
        updateWishlistIcon()
    }

    private fun setupUI() {
        currentItem?.let { item ->
            // Set title
            binding.titleTxt.text = item.title

            // Set description
            binding.descriptionTxt.text = if (item.description.isNotEmpty()) {
                item.description
            } else {
                item.extra.ifEmpty { "Không có mô tả" }
            }

            // Set price
            binding.priceTxt.text = formatVND(item.price)

            // Set rating
            binding.ratingTxt.text = String.format(Locale.getDefault(), "%.1f", item.rating)

            // Load image
            if (item.picUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(item.picUrl[0])
                    .into(binding.productImage)
            }

            // Set default size to Medium
            updateSizeButtons()
        }
    }

    private fun setupClickListeners() {
        // Back button
        binding.backBtn.setOnClickListener {
            finish()
        }

        // Size buttons
        binding.sizeSmallBtn.setOnClickListener {
            selectedSize = "Small"
            updateSizeButtons()
            updatePrice()
        }

        binding.sizeMediumBtn.setOnClickListener {
            selectedSize = "Medium"
            updateSizeButtons()
            updatePrice()
        }

        binding.sizeLargeBtn.setOnClickListener {
            selectedSize = "Large"
            updateSizeButtons()
            updatePrice()
        }

        // Quantity buttons
        binding.minusBtn.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.quantityTxt.text = quantity.toString()
                updatePrice()
            }
        }

        binding.plusBtn.setOnClickListener {
            quantity++
            binding.quantityTxt.text = quantity.toString()
            updatePrice()
        }

        // Add to cart button
        binding.addToCartBtn.setOnClickListener {
            addToCart()
        }

        // Favorite button
        binding.favoriteBtn.setOnClickListener {
            toggleWishlist()
        }
    }

    private fun updateSizeButtons() {
        // Reset all
        binding.sizeSmallBtn.setBackgroundResource(R.drawable.white_full_corner_bg)
        binding.sizeSmallBtn.setTextColor(getColor(R.color.darkBrown))
        binding.sizeMediumBtn.setBackgroundResource(R.drawable.white_full_corner_bg)
        binding.sizeMediumBtn.setTextColor(getColor(R.color.darkBrown))
        binding.sizeLargeBtn.setBackgroundResource(R.drawable.white_full_corner_bg)
        binding.sizeLargeBtn.setTextColor(getColor(R.color.darkBrown))

        // Set selected
        when (selectedSize) {
            "Small" -> {
                binding.sizeSmallBtn.setBackgroundResource(R.drawable.brown_full_corner_bg)
                binding.sizeSmallBtn.setTextColor(getColor(R.color.white))
            }
            "Medium" -> {
                binding.sizeMediumBtn.setBackgroundResource(R.drawable.brown_full_corner_bg)
                binding.sizeMediumBtn.setTextColor(getColor(R.color.white))
            }
            "Large" -> {
                binding.sizeLargeBtn.setBackgroundResource(R.drawable.brown_full_corner_bg)
                binding.sizeLargeBtn.setTextColor(getColor(R.color.white))
            }
        }
        updatePrice()
    }

    private fun updatePrice() {
        currentItem?.let { item ->
            var basePrice = item.price

            // Adjust price based on size
            when (selectedSize) {
                "Small" -> basePrice *= 0.9  // 10% discount for small
                "Medium" -> basePrice = item.price  // Original price
                "Large" -> basePrice *= 1.2  // 20% increase for large
            }

            val totalPrice = basePrice * quantity
            binding.priceTxt.text = formatVND(totalPrice)
        }
    }

    private fun addToCart() {
        currentItem?.let { item ->
            // Add item to cart with quantity
            var success = true
            for (i in 1..quantity) {
                if (!cartManager.addToCart(item)) {
                    success = false
                    break
                }
            }

            if (success) {
                Toast.makeText(
                    this,
                    "$quantity ${item.title} đã được thêm vào giỏ hàng",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(this, "Có lỗi xảy ra khi thêm vào giỏ hàng", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun toggleWishlist() {
        currentItem?.let { item ->
            val isInWishlist = wishlistManager.isInWishlist(item.title)

            if (isInWishlist) {
                // Remove from wishlist
                if (wishlistManager.removeFromWishlist(item.title)) {
                    Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show()
                    updateWishlistIcon()
                }
            } else {
                // Add to wishlist
                if (wishlistManager.addToWishlist(item)) {
                    Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show()
                    updateWishlistIcon()
                }
            }
        }
    }

    private fun updateWishlistIcon() {
        currentItem?.let { item ->
            val isInWishlist = wishlistManager.isInWishlist(item.title)
            // Change alpha to indicate wishlist status
            binding.favoriteBtn.alpha = if (isInWishlist) 1.0f else 0.6f
        }
    }
}