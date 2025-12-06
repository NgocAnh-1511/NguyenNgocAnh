package com.example.coffeeshop.Activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshop.Adapter.PopularAdapter
import com.example.coffeeshop.Adapter.SidebarCategoryAdapter
import com.example.coffeeshop.Domain.ItemsModel
import androidx.lifecycle.ViewModelProvider
import com.example.coffeeshop.Manager.CartManager
import com.example.coffeeshop.R
import com.example.coffeeshop.Utils.dpToPx
import com.example.coffeeshop.ViewModel.MainViewModel
import com.example.coffeeshop.databinding.ActivityOrderDrinkBinding

class OrderDrinkActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderDrinkBinding
    private lateinit var cartManager: CartManager
    private lateinit var viewModel: MainViewModel
    private lateinit var sidebarAdapter: SidebarCategoryAdapter
    private lateinit var productAdapter: PopularAdapter
    private var allItems: MutableList<ItemsModel> = mutableListOf()
    private var filteredItems: MutableList<ItemsModel> = mutableListOf()
    private var currentCategoryId: String? = null
    private var isDelivery: Boolean = true
    private var currentFilter: String = "all" // all, bestseller, musttry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOrderDrinkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle edge-to-edge insets for header
        ViewCompat.setOnApplyWindowInsetsListener(binding.headerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParams = v.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = systemBars.top + 8.dpToPx() // Đưa header xuống một chút
            v.layoutParams = layoutParams
            insets
        }

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        cartManager = CartManager(this)

        setupRecyclerViews()
        setupClickListeners()
        setupSearchListener()
        loadCategories()
        updateCartBadge()
        
        // Check if filter is passed from intent
        val filter = intent.getStringExtra("filter")
        if (filter != null) {
            currentFilter = filter
            updateFilterButtons()
            // Filter will be applied after items are loaded
        }
    }

    private fun setupRecyclerViews() {
        // Sidebar categories
        binding.sidebarCategoryView.layoutManager = LinearLayoutManager(this)
        sidebarAdapter = SidebarCategoryAdapter(mutableListOf()) { category ->
            if (category.id == -1) {
                currentCategoryId = null // Show all
            } else {
                currentCategoryId = category.id.toString()
            }
            filterProducts()
        }
        binding.sidebarCategoryView.adapter = sidebarAdapter

        // Product grid
        binding.recyclerViewProducts.layoutManager = GridLayoutManager(this, 2)
        productAdapter = PopularAdapter(
            mutableListOf(),
            onAddToCartClick = { item ->
                if (cartManager.addToCart(item)) {
                    Toast.makeText(this, "${item.title} đã được thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                    updateCartBadge()
                    // Animation
                    binding.cartBadgeOrder.scaleX = 1.5f
                    binding.cartBadgeOrder.scaleY = 1.5f
                    binding.cartBadgeOrder.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                } else {
                    Toast.makeText(this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                }
            }
        )
        binding.recyclerViewProducts.adapter = productAdapter
    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.cartIconBtn.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        // Delivery/Pickup toggle
        binding.deliveryBtn.setOnClickListener {
            if (!isDelivery) {
                isDelivery = true
                updateDeliveryToggle()
            }
        }

        binding.pickupBtn.setOnClickListener {
            if (isDelivery) {
                isDelivery = false
                updateDeliveryToggle()
            }
        }

        // Filter buttons
        binding.filterAllBtn.setOnClickListener {
            currentFilter = "all"
            updateFilterButtons()
            filterProducts()
        }

        binding.filterBestSellerBtn.setOnClickListener {
            currentFilter = "bestseller"
            updateFilterButtons()
            filterProducts()
        }

        binding.filterMustTryBtn.setOnClickListener {
            currentFilter = "musttry"
            updateFilterButtons()
            filterProducts()
        }
    }

    private fun updateDeliveryToggle() {
        if (isDelivery) {
            binding.deliveryBtn.setBackgroundResource(R.drawable.brown_full_corner_bg)
            binding.deliveryBtn.setTextColor(getColor(R.color.white))
            binding.pickupBtn.setBackgroundResource(android.R.color.transparent)
            binding.pickupBtn.setTextColor(getColor(R.color.darkBrown))
        } else {
            binding.deliveryBtn.setBackgroundResource(android.R.color.transparent)
            binding.deliveryBtn.setTextColor(getColor(R.color.darkBrown))
            binding.pickupBtn.setBackgroundResource(R.drawable.brown_full_corner_bg)
            binding.pickupBtn.setTextColor(getColor(R.color.white))
        }
    }

    private fun updateFilterButtons() {
        // Reset all
        binding.filterAllBtn.setBackgroundResource(R.drawable.white_full_corner_bg)
        binding.filterAllBtn.setTextColor(getColor(R.color.darkBrown))
        binding.filterBestSellerBtn.setBackgroundResource(R.drawable.white_full_corner_bg)
        binding.filterBestSellerBtn.setTextColor(getColor(R.color.darkBrown))
        binding.filterMustTryBtn.setBackgroundResource(R.drawable.white_full_corner_bg)
        binding.filterMustTryBtn.setTextColor(getColor(R.color.darkBrown))

        // Set selected
        when (currentFilter) {
            "all" -> {
                binding.filterAllBtn.setBackgroundResource(R.drawable.brown_full_corner_bg)
                binding.filterAllBtn.setTextColor(getColor(R.color.white))
            }
            "bestseller" -> {
                binding.filterBestSellerBtn.setBackgroundResource(R.drawable.brown_full_corner_bg)
                binding.filterBestSellerBtn.setTextColor(getColor(R.color.white))
            }
            "musttry" -> {
                binding.filterMustTryBtn.setBackgroundResource(R.drawable.brown_full_corner_bg)
                binding.filterMustTryBtn.setTextColor(getColor(R.color.white))
            }
        }
    }

    private fun setupSearchListener() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterProducts()
            }
        })
    }

    private fun loadCategories() {
        binding.progressBar.visibility = View.VISIBLE
        viewModel.loadCategory().observe(this) { categories ->
            if (categories != null && categories.isNotEmpty()) {
                // Add "All" category at the beginning
                val allCategories = mutableListOf<com.example.coffeeshop.Domain.CategoryModel>()
                allCategories.add(com.example.coffeeshop.Domain.CategoryModel("Tất cả", -1))
                allCategories.addAll(categories)
                
                sidebarAdapter.updateList(allCategories)
                sidebarAdapter.setSelected(0) // Select "Tất cả" by default
                currentCategoryId = null
                
                // Load all items
                loadAllItems(categories)
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun loadAllItems(categories: MutableList<com.example.coffeeshop.Domain.CategoryModel>) {
        allItems.clear()
        
        // Load popular items first
        viewModel.loadPopular().observe(this) { popularItems ->
            if (popularItems != null) {
                allItems.addAll(popularItems)
                
                // Load items from categories
                var loadedCount = 0
                val totalCategories = categories.size
                
                if (totalCategories == 0) {
                    binding.progressBar.visibility = View.GONE
                    filterProducts()
                    return@observe
                }
                
                categories.forEach { category ->
                    viewModel.loadItems(category.id.toString()).observe(this) { items ->
                        if (items != null) {
                            allItems.addAll(items.filter { item -> 
                                !allItems.any { it.title == item.title } 
                            })
                            loadedCount++
                            if (loadedCount == totalCategories) {
                                binding.progressBar.visibility = View.GONE
                                filterProducts()
                                // Add fade animation
                                binding.recyclerViewProducts.alpha = 0f
                                binding.recyclerViewProducts.animate().alpha(1f).setDuration(300).start()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun filterProducts() {
        var filtered = allItems.toMutableList()

        // Filter by category
        if (currentCategoryId != null && currentCategoryId != "-1") {
            filtered = filtered.filter { 
                it.categoryId == currentCategoryId
            }.toMutableList()
        }

        // Filter by search query
        val searchQuery = binding.searchEditText.text.toString().trim()
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.extra.contains(searchQuery, ignoreCase = true)
            }.toMutableList()
        }

        // Filter by type (bestseller, musttry)
        when (currentFilter) {
            "bestseller" -> {
                // Filter by rating >= 4.5
                filtered = filtered.filter { it.rating >= 4.5 }.toMutableList()
            }
            "musttry" -> {
                // Filter by rating >= 4.0
                filtered = filtered.filter { it.rating >= 4.0 }.toMutableList()
            }
        }

        filteredItems = filtered
        productAdapter.updateList(filteredItems)
    }

    private fun updateCartBadge() {
        val cartCount = cartManager.getCartItemCount()
        if (cartCount > 0) {
            binding.cartBadgeOrder.visibility = View.VISIBLE
            binding.cartBadgeOrder.text = if (cartCount > 99) "99+" else cartCount.toString()
        } else {
            binding.cartBadgeOrder.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        updateCartBadge()
    }
}

