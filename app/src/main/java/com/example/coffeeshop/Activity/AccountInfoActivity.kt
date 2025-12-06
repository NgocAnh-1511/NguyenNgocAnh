package com.example.coffeeshop.Activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.coffeeshop.Manager.UserManager
import com.example.coffeeshop.R
import com.example.coffeeshop.Utils.ValidationUtils
import com.example.coffeeshop.databinding.ActivityAccountInfoBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AccountInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountInfoBinding
    private lateinit var userManager: UserManager
    private var currentAvatarPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAccountInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userManager = UserManager(this)

        // Kiểm tra đăng nhập
        if (!userManager.isLoggedIn()) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        loadUserInfo()
        setupClickListeners()
    }

    // Activity result launcher để chọn ảnh từ gallery
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    // Lưu ảnh vào internal storage
                    val savedPath = saveImageToInternalStorage(uri)
                    if (savedPath.isNotEmpty()) {
                        currentAvatarPath = savedPath
                        // Cập nhật user với avatar mới
                        val currentUser = userManager.getCurrentUser()
                        if (currentUser != null) {
                            lifecycleScope.launch {
                                val success = userManager.updateUser(
                                    currentUser.userId,
                                    avatarPath = savedPath
                                )
                                if (success) {
                                    // Hiển thị ảnh đã chọn
                                    loadAvatar(savedPath)
                                    Toast.makeText(this@AccountInfoActivity, "Đã cập nhật ảnh đại diện!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Lỗi khi tải ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Cập nhật lại thông tin khi quay lại màn hình
        loadUserInfo()
    }

    private fun loadUserInfo() {
        val user = userManager.getCurrentUser()
        if (user != null) {
            // Load và hiển thị ảnh đại diện
            currentAvatarPath = user.avatarPath
            if (user.avatarPath.isNotEmpty()) {
                loadAvatar(user.avatarPath)
            } else {
                binding.ivProfileAvatar.setImageResource(R.drawable.profile)
            }
            
            // Hiển thị thông tin đầy đủ
            binding.tvFullName.text = if (user.fullName.isNotEmpty()) {
                user.fullName
            } else {
                "Chưa cập nhật"
            }
            
            binding.tvPhoneNumber.text = user.phoneNumber
            
            binding.tvEmail.text = if (user.email.isNotEmpty()) {
                user.email
            } else {
                "Chưa cập nhật"
            }
            
            // Format thời gian tạo
            if (user.createdAt > 0) {
                val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                binding.tvCreatedAt.text = dateFormat.format(java.util.Date(user.createdAt))
            } else {
                binding.tvCreatedAt.text = "Chưa có"
            }
        }
    }
    
    private fun loadAvatar(imagePath: String) {
        val imageFile = File(filesDir, imagePath)
        if (imageFile.exists()) {
            Glide.with(this)
                .load(imageFile)
                .circleCrop()
                .into(binding.ivProfileAvatar)
        } else {
            binding.ivProfileAvatar.setImageResource(R.drawable.profile)
        }
    }
    
    private fun saveImageToInternalStorage(uri: Uri): String {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            var bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            // Xử lý EXIF orientation để ảnh hiển thị đúng hướng
            try {
                val exifInputStream = contentResolver.openInputStream(uri)
                val exif = exifInputStream?.let { 
                    ExifInterface(it)
                }
                exifInputStream?.close()
                
                val orientation = exif?.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                ) ?: ExifInterface.ORIENTATION_NORMAL
                
                val matrix = Matrix()
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                    ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                }
                
                if (orientation != ExifInterface.ORIENTATION_NORMAL) {
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                }
            } catch (e: Exception) {
                // Nếu không đọc được EXIF, bỏ qua
            }
            
            // Resize và crop ảnh về dạng vuông để tránh bị xoay
            val size = minOf(bitmap.width, bitmap.height)
            val x = (bitmap.width - size) / 2
            val y = (bitmap.height - size) / 2
            bitmap = Bitmap.createBitmap(bitmap, x, y, size, size)
            
            // Resize về kích thước hợp lý (512x512) để tiết kiệm dung lượng
            val maxSize = 512
            if (size > maxSize) {
                bitmap = Bitmap.createScaledBitmap(bitmap, maxSize, maxSize, true)
            }
            
            // Tạo tên file unique
            val userId = userManager.getCurrentUser()?.userId ?: System.currentTimeMillis().toString()
            val fileName = "avatar_$userId.jpg"
            val file = File(filesDir, "avatars")
            if (!file.exists()) {
                file.mkdirs()
            }
            
            val avatarFile = File(file, fileName)
            val outputStream = FileOutputStream(avatarFile)
            
            // Compress và lưu ảnh
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.flush()
            outputStream.close()
            
            // Trả về relative path
            return "avatars/$fileName"
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    private fun setupClickListeners() {
        // Nút quay lại - chỉ cần finish()
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        // Click vào avatar hoặc icon camera để chọn ảnh
        binding.ivProfileAvatar.setOnClickListener {
            openImagePicker()
        }
        
        binding.ivCameraIcon.setOnClickListener {
            openImagePicker()
        }

        // Click vào tên để chỉnh sửa
        binding.tvFullName.setOnClickListener {
            showEditNameDialog()
        }

        // Click vào email để chỉnh sửa
        binding.tvEmail.setOnClickListener {
            showEditEmailDialog()
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
            finish() // Quay về ProfileActivity
        }
    }

    private fun showEditNameDialog() {
        val currentUser = userManager.getCurrentUser() ?: return
        
        // Tách tên hiện tại thành họ & tên đệm và tên
        val fullName = currentUser.fullName
        val nameParts = fullName.trim().split("\\s+".toRegex())
        val lastName = if (nameParts.size >= 2) nameParts.dropLast(1).joinToString(" ") else ""
        val firstName = if (nameParts.isNotEmpty()) nameParts.last() else ""
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_name, null)
        val etLastName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etLastName)
        val etFirstName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etFirstName)
        
        etLastName.setText(lastName)
        etFirstName.setText(firstName)
        
        AlertDialog.Builder(this)
            .setTitle("Chỉnh sửa họ tên")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val newLastName = etLastName.text.toString().trim()
                val newFirstName = etFirstName.text.toString().trim()
                
                // Validation
                val lastNameValidation = ValidationUtils.validateLastName(newLastName)
                if (!lastNameValidation.first) {
                    Toast.makeText(this, lastNameValidation.second, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                val firstNameValidation = ValidationUtils.validateFirstName(newFirstName)
                if (!firstNameValidation.first) {
                    Toast.makeText(this, firstNameValidation.second, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                // Cập nhật
                val newFullName = "$newLastName $newFirstName"
                lifecycleScope.launch {
                    val success = userManager.updateUser(
                        currentUser.userId,
                        fullName = newFullName
                    )
                    if (success) {
                        Toast.makeText(this@AccountInfoActivity, "Cập nhật tên thành công!", Toast.LENGTH_SHORT).show()
                        loadUserInfo()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showEditEmailDialog() {
        val currentUser = userManager.getCurrentUser() ?: return
        val currentEmail = currentUser.email
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_email, null)
        val etEmail = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEmail)
        
        etEmail.setText(currentEmail)
        
        AlertDialog.Builder(this)
            .setTitle("Chỉnh sửa email")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val newEmail = etEmail.text.toString().trim()
                
                // Validation
                val emailValidation = ValidationUtils.validateEmail(newEmail)
                if (!emailValidation.first) {
                    Toast.makeText(this, emailValidation.second, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                // Cập nhật
                lifecycleScope.launch {
                    val success = userManager.updateUser(
                        currentUser.userId,
                        email = newEmail
                    )
                    if (success) {
                        Toast.makeText(this@AccountInfoActivity, "Cập nhật email thành công!", Toast.LENGTH_SHORT).show()
                        loadUserInfo()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }
}

