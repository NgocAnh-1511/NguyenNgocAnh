package com.example.coffeeshop.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.bumptech.glide.Glide
import com.example.coffeeshop.Manager.OrderManager
import com.example.coffeeshop.Manager.UserManager
import com.example.coffeeshop.R
import com.example.coffeeshop.Utils.dpToPx
import com.example.coffeeshop.Utils.formatVND
import com.example.coffeeshop.databinding.ActivityProfileBinding
import java.io.File
import java.util.Locale

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var userManager: UserManager
    private lateinit var orderManager: OrderManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle edge-to-edge insets for header
        ViewCompat.setOnApplyWindowInsetsListener(binding.headerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParams = v.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = systemBars.top + 8.dpToPx() // Đưa header xuống một chút
            v.layoutParams = layoutParams
            insets
        }

        userManager = UserManager(this)
        orderManager = OrderManager(this)

        // Không bắt buộc đăng nhập, nhưng nếu chưa đăng nhập thì hiển thị thông báo
        if (!userManager.isLoggedIn()) {
            binding.userNameTxt.text = "Khách"
            // Hiển thị thống kê = 0
            binding.totalOrdersTxt.text = "0"
            binding.completedOrdersTxt.text = "0"
            binding.totalSpentTxt.text = formatVND(0.0)
        } else {
            loadUserInfo()
            loadStatistics()
        }
        
        updateLogoutButton()
        setupClickListeners()
        updateAdminSection()
    }

    override fun onResume() {
        super.onResume()
        // Cập nhật lại thông tin khi quay lại màn hình (ví dụ sau khi hoàn thiện thông tin)
        if (userManager.isLoggedIn()) {
            loadUserInfo()
            loadStatistics()
        } else {
            binding.userNameTxt.text = "Khách"
            // Hiển thị thống kê = 0
            binding.totalOrdersTxt.text = "0"
            binding.completedOrdersTxt.text = "0"
            binding.totalSpentTxt.text = formatVND(0.0)
        }
        updateLogoutButton()
        updateAdminSection()
    }
    
    private fun updateAdminSection() {
        if (userManager.isLoggedIn() && userManager.isAdmin()) {
            binding.adminSectionCard.visibility = View.VISIBLE
        } else {
            binding.adminSectionCard.visibility = View.GONE
        }
    }
    
    private fun updateLogoutButton() {
        if (userManager.isLoggedIn()) {
            // Hiển thị "Đăng xuất" khi đã đăng nhập
            binding.logoutBtn.text = "Đăng xuất"
        } else {
            // Hiển thị "Đăng nhập/Đăng ký" khi chưa đăng nhập
            binding.logoutBtn.text = "Đăng nhập/Đăng ký"
        }
    }

    private fun loadUserInfo() {
        val user = userManager.getCurrentUser()
        if (user != null) {
            val displayName = if (user.fullName.isNotEmpty()) {
                user.fullName
            } else {
                user.phoneNumber
            }
            
            // Hiển thị tên
            binding.userNameTxt.text = displayName
            
            // Load và hiển thị ảnh đại diện
            if (user.avatarPath.isNotEmpty()) {
                val imageFile = File(filesDir, user.avatarPath)
                if (imageFile.exists()) {
                    Glide.with(this)
                        .load(imageFile)
                        .circleCrop()
                        .into(binding.ivProfileAvatar)
                } else {
                    binding.ivProfileAvatar.setImageResource(R.drawable.profile)
                }
            } else {
                binding.ivProfileAvatar.setImageResource(R.drawable.profile)
            }
        } else {
            binding.userNameTxt.text = "Khách"
        }
    }

    private fun loadStatistics() {
        lifecycleScope.launch {
            val orders = orderManager.getAllOrders()
        val totalOrders = orders.size
        val completedOrders = orders.filter { it.status.equals("Completed", ignoreCase = true) }
        val totalSpent = completedOrders.sumOf { it.totalPrice }
        
        // Hiển thị thống kê
        binding.totalOrdersTxt.text = totalOrders.toString()
        binding.completedOrdersTxt.text = completedOrders.size.toString()
        binding.totalSpentTxt.text = formatVND(totalSpent)
        }
    }

    private fun setupClickListeners() {
        // Back button
        binding.backBtn.setOnClickListener {
            finish()
        }

        // List view click listeners
        binding.myOrdersBtnList.setOnClickListener {
            if (!userManager.isLoggedIn()) {
                Toast.makeText(this, "Vui lòng đăng nhập để xem đơn hàng", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, OrderActivity::class.java)
                startActivity(intent)
            }
        }

        binding.addressBtnInProfileList.setOnClickListener {
            if (!userManager.isLoggedIn()) {
                Toast.makeText(this, "Vui lòng đăng nhập để quản lý địa chỉ", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, AddressListActivity::class.java)
                startActivity(intent)
            }
        }

        binding.wishlistBtnInProfileList.setOnClickListener {
            val intent = Intent(this, WishlistActivity::class.java)
            startActivity(intent)
        }

        binding.vouchersBtnList.setOnClickListener {
            val intent = Intent(this, VoucherListActivity::class.java)
            startActivity(intent)
        }

        // Complete Profile button
        binding.completeProfileBtnList.setOnClickListener {
            if (!userManager.isLoggedIn()) {
                Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, CompleteProfileActivity::class.java)
                startActivity(intent)
            }
        }

        // Change Password button
        binding.changePasswordBtnList.setOnClickListener {
            if (!userManager.isLoggedIn()) {
                Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, ChangePasswordActivity::class.java)
                startActivity(intent)
            }
        }

        // Admin functions (only visible for admin users)
        if (userManager.isLoggedIn() && userManager.isAdmin()) {
            binding.adminSectionCard.visibility = View.VISIBLE
            
            binding.adminOrderBtnList.setOnClickListener {
                val intent = Intent(this, AdminOrderActivity::class.java)
                startActivity(intent)
            }
            
            binding.adminVoucherBtnList.setOnClickListener {
                val intent = Intent(this, AdminVoucherActivity::class.java)
                startActivity(intent)
            }
        } else {
            binding.adminSectionCard.visibility = View.GONE
        }

        binding.logoutBtn.setOnClickListener {
            if (userManager.isLoggedIn()) {
                // Đăng xuất
                userManager.logout()
                userManager.clearToken()
                Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
                
                // Chuyển về profile khách
                binding.userNameTxt.text = "Khách"
                binding.totalOrdersTxt.text = "0"
                binding.completedOrdersTxt.text = "0"
                binding.totalSpentTxt.text = formatVND(0.0)
                updateLogoutButton()
            } else {
                // Chưa đăng nhập, chuyển đến màn hình đăng nhập/đăng ký
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }
}

