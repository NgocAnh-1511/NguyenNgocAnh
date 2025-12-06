package com.example.coffeeshop.Activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import com.example.coffeeshop.Adapter.CartAdapter
import com.example.coffeeshop.Domain.AddressModel
import com.example.coffeeshop.Manager.AddressManager
import com.example.coffeeshop.Manager.CartManager
import com.example.coffeeshop.Manager.OrderManager
import com.example.coffeeshop.Manager.UserManager
import com.example.coffeeshop.Manager.VoucherManager
import com.example.coffeeshop.R
import com.example.coffeeshop.Utils.ValidationUtils
import com.example.coffeeshop.Utils.formatVND
import com.example.coffeeshop.databinding.ActivityCheckoutBinding
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var cartManager: CartManager
    private lateinit var orderManager: OrderManager
    private lateinit var addressManager: AddressManager
    private lateinit var userManager: UserManager
    private lateinit var voucherManager: VoucherManager
    private lateinit var cartAdapter: CartAdapter
    private var appliedVoucher: com.example.coffeeshop.Domain.VoucherModel? = null
    private var baseTotal: Double = 0.0
    private var hasSelectedAddress: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userManager = UserManager(this)
        cartManager = CartManager(this)
        orderManager = OrderManager(this)
        addressManager = AddressManager(this)
        voucherManager = VoucherManager(this)

        // Bắt buộc phải đăng nhập để thanh toán
        if (!userManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để thanh toán", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("redirectToCheckout", true)
            startActivity(intent)
            finish()
            return
        }

        setupRecyclerView()
        setupClickListeners()
        loadOrderSummary()
        loadSavedAddresses()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewOrderSummary.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(
            mutableListOf(),
            onQuantityChanged = { _, _ -> }, // Read-only in checkout
            onRemoveClick = { }, // Read-only in checkout
            isReadOnly = true // Set read-only mode
        )
        binding.recyclerViewOrderSummary.adapter = cartAdapter
    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.confirmPaymentBtn.setOnClickListener {
            if (validateInput()) {
                processPayment()
            }
        }

        binding.selectVoucherBtn.setOnClickListener {
            selectVoucher()
        }

        binding.selectAddressBtn.setOnClickListener {
            selectSavedAddress()
        }
    }

    private fun loadSavedAddresses() {
        val defaultAddress = addressManager.getDefaultAddress()
        defaultAddress?.let {
            binding.nameEditText.setText(it.name)
            binding.phoneEditText.setText(it.phone)
            binding.addressEditText.setText(it.address)
        }
    }

    private val addressResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val addressJson = result.data?.getStringExtra("selectedAddress")
            if (addressJson != null) {
                val address = com.google.gson.Gson().fromJson(addressJson, AddressModel::class.java)
                binding.nameEditText.setText(address.name)
                binding.phoneEditText.setText(address.phone)
                binding.addressEditText.setText(address.address)
                hasSelectedAddress = true // Đánh dấu đã chọn địa chỉ
                Toast.makeText(this, "Đã chọn địa chỉ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val voucherResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val voucherJson = result.data?.getStringExtra("selectedVoucher")
            if (voucherJson != null) {
                val voucher = com.google.gson.Gson().fromJson(voucherJson, com.example.coffeeshop.Domain.VoucherModel::class.java)
                applySelectedVoucher(voucher)
            }
        }
    }

    private fun selectSavedAddress() {
        val intent = Intent(this, AddressListActivity::class.java)
        intent.putExtra("selectMode", true)
        addressResultLauncher.launch(intent)
    }

    private fun loadOrderSummary() {
        val cartList = cartManager.getCartList()
        
        if (cartList.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        cartAdapter.updateList(cartList)
        baseTotal = cartManager.getTotalPrice()
        updateTotalPrice()
    }

    private fun updateTotalPrice() {
        binding.subtotalTxt.text = formatVND(baseTotal)
        
        // Tính giảm giá từ voucher
        val discount = if (appliedVoucher != null) {
            appliedVoucher!!.calculateDiscount(baseTotal)
        } else {
            0.0
        }
        
        val finalTotal = baseTotal - discount
        
        if (discount > 0 && appliedVoucher != null) {
            binding.voucherDiscountLayout.visibility = View.VISIBLE
            binding.discountTxt.text = "-${formatVND(discount)}"
        } else {
            binding.voucherDiscountLayout.visibility = View.GONE
        }
        
        binding.totalPriceTxt.text = formatVND(finalTotal)
    }

    private fun selectVoucher() {
        val intent = Intent(this, VoucherListActivity::class.java)
        intent.putExtra("selectMode", true)
        intent.putExtra("orderAmount", baseTotal)
        voucherResultLauncher.launch(intent)
    }

    private fun applySelectedVoucher(voucher: com.example.coffeeshop.Domain.VoucherModel) {
        lifecycleScope.launch {
            // Validate voucher again with current order amount
            val validatedVoucher = voucherManager.validateVoucher(voucher.code, baseTotal)
            
            if (validatedVoucher != null) {
                appliedVoucher = validatedVoucher
                
                val discountText = validatedVoucher.getDescriptionText()
                binding.voucherAppliedTxt.text = "Mã giảm giá đã được áp dụng: $discountText"
                binding.voucherAppliedTxt.visibility = View.VISIBLE
                binding.selectVoucherBtn.text = "Đổi mã giảm giá"
                binding.selectVoucherBtn.visibility = View.VISIBLE
                updateTotalPrice()
                Toast.makeText(this@CheckoutActivity, "Áp dụng mã giảm giá thành công!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@CheckoutActivity, "Mã giảm giá không hợp lệ hoặc đã hết hạn", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInput(): Boolean {
        val name = binding.nameEditText.text.toString().trim()
        val phone = binding.phoneEditText.text.toString().trim()
        val address = binding.addressEditText.text.toString().trim()

        // Validation tên
        val nameValidation = ValidationUtils.validateName(name)
        if (!nameValidation.first) {
            binding.nameEditText.error = nameValidation.second
            binding.nameEditText.requestFocus()
            return false
        }

        // Validation số điện thoại
        val phoneValidation = ValidationUtils.validatePhone(phone)
        if (!phoneValidation.first) {
            binding.phoneEditText.error = phoneValidation.second
            binding.phoneEditText.requestFocus()
            return false
        }

        // Validation địa chỉ
        if (TextUtils.isEmpty(address)) {
            binding.addressEditText.error = "Vui lòng nhập địa chỉ giao hàng"
            binding.addressEditText.requestFocus()
            return false
        }

        return true
    }

    private fun getPaymentMethod(): String {
        return when (binding.paymentMethodGroup.checkedRadioButtonId) {
            R.id.cashRadioBtn -> "Tiền mặt"
            R.id.cardRadioBtn -> "Thẻ tín dụng/Ghi nợ"
            R.id.eWalletRadioBtn -> "Ví điện tử"
            else -> "Tiền mặt"
        }
    }

    private fun processPayment() {
        val cartList = cartManager.getCartList()
        val discount = if (appliedVoucher != null) {
            appliedVoucher!!.calculateDiscount(baseTotal)
        } else {
            0.0
        }
        val totalPrice = baseTotal - discount
        
        val name = binding.nameEditText.text.toString().trim()
        val phone = binding.phoneEditText.text.toString().trim()
        val address = binding.addressEditText.text.toString().trim()
        val paymentMethod = getPaymentMethod()
        val voucherCode = appliedVoucher?.code ?: ""

        // Show loading
        binding.confirmPaymentBtn.isEnabled = false
        binding.confirmPaymentBtn.text = "Đang xử lý..."

        lifecycleScope.launch {
            try {
                android.util.Log.d("CheckoutActivity", "=== Processing Payment ===")
                android.util.Log.d("CheckoutActivity", "Cart items: ${cartList.size}")
                android.util.Log.d("CheckoutActivity", "Total price: $totalPrice")
                
                // Tăng số lần sử dụng voucher nếu có
                appliedVoucher?.let {
                    android.util.Log.d("CheckoutActivity", "Incrementing voucher usage: ${it.voucherId}")
                    voucherManager.incrementUsageCount(it.voucherId)
                }
                
                // Create order
                android.util.Log.d("CheckoutActivity", "Creating order...")
                val order = orderManager.createOrder(
                    items = cartList,
                    totalPrice = totalPrice,
                    deliveryAddress = address,
                    phoneNumber = phone,
                    customerName = name,
                    paymentMethod = paymentMethod
                )

                if (order != null) {
                    android.util.Log.d("CheckoutActivity", "Order created successfully: ${order.orderId}")
                    
                    // Clear cart
                    cartManager.clearCart()

                    // Show success message and navigate to order detail
                    Toast.makeText(
                        this@CheckoutActivity,
                        "Thanh toán thành công! Mã đơn: #${order.orderId.take(8)}",
                        Toast.LENGTH_LONG
                    ).show()

                    // Navigate to order detail
                    val intent = Intent(this@CheckoutActivity, OrderDetailActivity::class.java)
                    val orderJson = com.google.gson.Gson().toJson(order)
                    intent.putExtra("order", orderJson)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    
                    // Finish checkout and cart activities
                    finish()
                } else {
                    android.util.Log.e("CheckoutActivity", "Order creation returned null")
                    binding.confirmPaymentBtn.isEnabled = true
                    binding.confirmPaymentBtn.text = "Xác nhận thanh toán"
                    Toast.makeText(this@CheckoutActivity, "Tạo đơn hàng thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("CheckoutActivity", "Payment processing error", e)
                e.printStackTrace()
                binding.confirmPaymentBtn.isEnabled = true
                binding.confirmPaymentBtn.text = "Xác nhận thanh toán"
                val errorMsg = e.message ?: "Lỗi không xác định"
                Toast.makeText(this@CheckoutActivity, "Lỗi: $errorMsg", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel any pending operations
        binding.root.removeCallbacks(null)
    }

    override fun onResume() {
        super.onResume()
        // Chỉ load lại địa chỉ mặc định nếu các trường địa chỉ đang trống
        // Tránh ghi đè địa chỉ vừa chọn từ AddressListActivity
        val nameEmpty = binding.nameEditText.text.toString().trim().isEmpty()
        val phoneEmpty = binding.phoneEditText.text.toString().trim().isEmpty()
        val addressEmpty = binding.addressEditText.text.toString().trim().isEmpty()
        
        if (nameEmpty && phoneEmpty && addressEmpty && !hasSelectedAddress) {
            loadSavedAddresses()
        }
    }
}

