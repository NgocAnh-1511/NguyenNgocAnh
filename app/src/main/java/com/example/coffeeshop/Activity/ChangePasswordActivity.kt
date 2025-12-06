package com.example.coffeeshop.Activity

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.coffeeshop.Manager.UserManager
import com.example.coffeeshop.R
import com.example.coffeeshop.Utils.ValidationUtils
import com.example.coffeeshop.Utils.dpToPx
import com.example.coffeeshop.databinding.ActivityChangePasswordBinding

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userManager = UserManager(this)

        // Kiểm tra đăng nhập
        if (!userManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Change password button
        binding.btnChangePassword.setOnClickListener {
            val currentPassword = binding.etCurrentPassword.text.toString()
            val newPassword = binding.etNewPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (currentPassword.isEmpty()) {
                binding.etCurrentPassword.error = "Vui lòng nhập mật khẩu hiện tại"
                binding.etCurrentPassword.requestFocus()
                return@setOnClickListener
            }
            
            // Validation mật khẩu mới
            val passwordValidation = ValidationUtils.validatePassword(newPassword)
            if (!passwordValidation.first) {
                binding.etNewPassword.error = passwordValidation.second
                binding.etNewPassword.requestFocus()
                return@setOnClickListener
            }
            
            // Validation xác nhận mật khẩu
            val confirmPasswordValidation = ValidationUtils.validateConfirmPassword(newPassword, confirmPassword)
            if (!confirmPasswordValidation.first) {
                binding.etConfirmPassword.error = confirmPasswordValidation.second
                binding.etConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            // Gọi API để cập nhật mật khẩu
            val currentUser = userManager.getCurrentUser()
            if (currentUser == null) {
                Toast.makeText(this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                return@setOnClickListener
            }

            // Disable button để tránh click nhiều lần
            binding.btnChangePassword.isEnabled = false
            binding.btnChangePassword.text = "Đang xử lý..."

            lifecycleScope.launch {
                try {
                    val success = userManager.updateUser(
                        userId = currentUser.userId,
                        password = newPassword
                    )
                    
                    binding.btnChangePassword.isEnabled = true
                    binding.btnChangePassword.text = "Đổi mật khẩu"
                    
                    if (success) {
                        Toast.makeText(this@ChangePasswordActivity, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
                        // Xóa các field sau khi thành công
                        binding.etCurrentPassword.text?.clear()
                        binding.etNewPassword.text?.clear()
                        binding.etConfirmPassword.text?.clear()
                        finish()
                    } else {
                        Toast.makeText(this@ChangePasswordActivity, "Đổi mật khẩu thất bại. Vui lòng kiểm tra lại thông tin và thử lại.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ChangePasswordActivity", "Error changing password", e)
                    binding.btnChangePassword.isEnabled = true
                    binding.btnChangePassword.text = "Đổi mật khẩu"
                    Toast.makeText(this@ChangePasswordActivity, "Có lỗi xảy ra: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Bottom Navigation
        binding.btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        binding.btnCart.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        binding.btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}

