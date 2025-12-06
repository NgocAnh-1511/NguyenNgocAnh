package com.example.coffeeshop.Activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import com.example.coffeeshop.Adapter.VoucherAdapter
import com.example.coffeeshop.Manager.VoucherManager
import com.example.coffeeshop.R
import com.example.coffeeshop.Utils.dpToPx
import com.example.coffeeshop.databinding.ActivityVoucherListBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VoucherListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVoucherListBinding
    private lateinit var voucherManager: VoucherManager
    private lateinit var voucherAdapter: VoucherAdapter
    private var currentFilter: FilterType = FilterType.AVAILABLE
    private var isSelectMode: Boolean = false
    private var orderAmount: Double = 0.0

    enum class FilterType {
        AVAILABLE,    // Khả dụng
        UNAVAILABLE   // Không khả dụng
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVoucherListBinding.inflate(layoutInflater)
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

        // Check if in select mode (from CheckoutActivity)
        isSelectMode = intent.getBooleanExtra("selectMode", false)
        orderAmount = intent.getDoubleExtra("orderAmount", 0.0)

        setupRecyclerView()
        setupClickListeners()
        setupSearchInput()
        loadVouchers()
        
        // If in select mode, only show available vouchers
        if (isSelectMode) {
            currentFilter = FilterType.AVAILABLE
            updateTabSelection()
            loadVouchers()
            // Hide search input and tabs in select mode
            binding.searchInputLayout.visibility = View.GONE
            binding.filterTabsLayout.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewVouchers.layoutManager = LinearLayoutManager(this)
        voucherAdapter = VoucherAdapter(mutableListOf()) { voucher ->
            if (isSelectMode) {
                // Return selected voucher to CheckoutActivity
                val voucherJson = com.google.gson.Gson().toJson(voucher)
                val resultIntent = Intent().apply {
                    putExtra("selectedVoucher", voucherJson)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                // Just show toast in normal view mode
                Toast.makeText(this, "Đã chọn mã: ${voucher.code}", Toast.LENGTH_SHORT).show()
            }
        }
        binding.recyclerViewVouchers.adapter = voucherAdapter
    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.availableTab.setOnClickListener {
            currentFilter = FilterType.AVAILABLE
            updateTabSelection()
            loadVouchers()
        }

        binding.unavailableTab.setOnClickListener {
            currentFilter = FilterType.UNAVAILABLE
            updateTabSelection()
            loadVouchers()
        }

        // Search will be handled by TextWatcher
    }

    private fun setupSearchInput() {
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val code = binding.searchEditText.text.toString().trim().uppercase()
                if (code.isNotEmpty()) {
                    searchVoucher(code)
                } else {
                    loadVouchers()
                }
                true
            } else {
                false
            }
        }
        
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    loadVouchers()
                }
            }
        })
    }

    private fun updateTabSelection() {
        if (currentFilter == FilterType.AVAILABLE) {
            binding.availableTab.setBackgroundResource(R.drawable.tab_selected_bg)
            binding.availableTab.setTextColor(getColor(R.color.white))
            binding.unavailableTab.setBackgroundResource(R.drawable.tab_unselected_bg)
            binding.unavailableTab.setTextColor(getColor(R.color.grey))
        } else {
            binding.availableTab.setBackgroundResource(R.drawable.tab_unselected_bg)
            binding.availableTab.setTextColor(getColor(R.color.grey))
            binding.unavailableTab.setBackgroundResource(R.drawable.tab_selected_bg)
            binding.unavailableTab.setTextColor(getColor(R.color.white))
        }
    }

    private fun loadVouchers() {
        lifecycleScope.launch {
            val allVouchers = voucherManager.getAllVouchers()
        val currentTime = System.currentTimeMillis()
        
        val filteredVouchers = when (currentFilter) {
            FilterType.AVAILABLE -> {
                allVouchers.filter { voucher ->
                    val isValid = voucher.isValid(currentTime)
                    // In select mode, also check if voucher is valid for order amount
                    if (isSelectMode && orderAmount > 0) {
                        isValid && orderAmount >= voucher.minOrderAmount
                    } else {
                        isValid
                    }
                }
            }
            FilterType.UNAVAILABLE -> {
                allVouchers.filter { voucher ->
                    !voucher.isValid(currentTime)
                }
            }
        }
        
        voucherAdapter.updateList(filteredVouchers.toMutableList())
        
        if (filteredVouchers.isEmpty()) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.recyclerViewVouchers.visibility = View.GONE
        } else {
            binding.emptyStateLayout.visibility = View.GONE
            binding.recyclerViewVouchers.visibility = View.VISIBLE
        }
        }
    }

    private fun searchVoucher(code: String) {
        lifecycleScope.launch {
            val voucher = voucherManager.getVoucherByCode(code)
        if (voucher != null) {
            val currentTime = System.currentTimeMillis()
            val isValid = voucher.isValid(currentTime)
            
            if (isValid) {
                currentFilter = FilterType.AVAILABLE
            } else {
                currentFilter = FilterType.UNAVAILABLE
            }
            
            updateTabSelection()
            voucherAdapter.updateList(mutableListOf(voucher))
            binding.emptyStateLayout.visibility = View.GONE
            binding.recyclerViewVouchers.visibility = View.VISIBLE
        } else {
            Toast.makeText(this@VoucherListActivity, "Không tìm thấy mã giảm giá", Toast.LENGTH_SHORT).show()
            loadVouchers()
        }
        }
    }

    override fun onResume() {
        super.onResume()
        loadVouchers()
    }
}

