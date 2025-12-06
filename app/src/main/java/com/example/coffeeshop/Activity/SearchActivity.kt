package com.example.coffeeshop.Activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.coffeeshop.Adapter.PopularAdapter
import com.example.coffeeshop.Domain.ItemsModel
import com.example.coffeeshop.Manager.CartManager
import com.example.coffeeshop.R
import com.example.coffeeshop.Utils.dpToPx
import androidx.lifecycle.ViewModelProvider
import com.example.coffeeshop.ViewModel.MainViewModel
import com.example.coffeeshop.databinding.ActivitySearchBinding
import android.widget.Toast

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var cartManager: CartManager
    private lateinit var viewModel: MainViewModel
    private var allItems: MutableList<ItemsModel> = mutableListOf()
    private lateinit var searchAdapter: PopularAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
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

        setupRecyclerView()
        setupClickListeners()
        loadAllItems()
        setupSearchListener()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewSearch.layoutManager = GridLayoutManager(this, 2)
        searchAdapter = PopularAdapter(
            mutableListOf(),
            onAddToCartClick = { item ->
                if (cartManager.addToCart(item)) {
                    Toast.makeText(this, "${item.title} đã được thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                }
            }
        )
        binding.recyclerViewSearch.adapter = searchAdapter
    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.clearSearchBtn.setOnClickListener {
            binding.searchEditText.text?.clear()
        }
    }

    private fun loadAllItems() {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE
        
        // Load popular items
        viewModel.loadPopular().observe(this) { popularItems ->
            if (popularItems != null) {
                allItems.clear()
                allItems.addAll(popularItems)
                
                // Load items from all categories
                viewModel.loadCategory().observe(this) { categories ->
                    if (categories != null && categories.isNotEmpty()) {
                        var loadedCount = 0
                        val totalCategories = categories.size
                        
                        categories.forEach { category ->
                            viewModel.loadItems(category.id.toString()).observe(this) { items ->
                                if (items != null) {
                                    allItems.addAll(items)
                                    loadedCount++
                                    if (loadedCount == totalCategories) {
                                        binding.progressBar.visibility = View.GONE
                                        filterItems("")
                                    }
                                }
                            }
                        }
                    } else {
                        binding.progressBar.visibility = View.GONE
                        filterItems("")
                    }
                }
            }
        }
    }

    private fun setupSearchListener() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterItems(s?.toString() ?: "")
            }
        })
    }

    private fun filterItems(query: String) {
        val filtered = if (query.isBlank()) {
            allItems
        } else {
            allItems.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.extra.contains(query, ignoreCase = true)
            }
        }

        if (filtered.isEmpty() && query.isNotBlank()) {
            binding.recyclerViewSearch.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
            binding.emptyStateText.text = "Không tìm thấy sản phẩm nào"
        } else {
            binding.recyclerViewSearch.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
        }

        // Show/hide clear button
        binding.clearSearchBtn.visibility = if (query.isNotBlank()) View.VISIBLE else View.GONE
        
        searchAdapter.updateList(filtered.toMutableList())
    }
}

