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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.bumptech.glide.Glide
import com.example.coffeeshop.Domain.UserModel
import com.example.coffeeshop.Manager.UserManager
import com.example.coffeeshop.R
import com.example.coffeeshop.Utils.ValidationUtils
import com.example.coffeeshop.databinding.ActivityCompleteProfileBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CompleteProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCompleteProfileBinding
    private lateinit var userManager: UserManager
    private var currentAvatarPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCompleteProfileBinding.inflate(layoutInflater)
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

        loadCurrentUserInfo()
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
                        // Hiển thị ảnh đã chọn
                        loadAvatar(savedPath)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Lỗi khi tải ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun loadCurrentUserInfo() {
        val currentUser = userManager.getCurrentUser()
        if (currentUser != null) {
            // Load avatar nếu có
            if (currentUser.avatarPath.isNotEmpty()) {
                currentAvatarPath = currentUser.avatarPath
                loadAvatar(currentUser.avatarPath)
            }
            
            // Nếu đã có fullName, tách ra thành lastName và firstName
            if (currentUser.fullName.isNotEmpty()) {
                val nameParts = currentUser.fullName.trim().split("\\s+".toRegex())
                if (nameParts.size >= 2) {
                    // Tất cả trừ từ cuối là họ & tên đệm
                    val lastName = nameParts.dropLast(1).joinToString(" ")
                    // Từ cuối là tên
                    val firstName = nameParts.last()
                    binding.etLastName.setText(lastName)
                    binding.etFirstName.setText(firstName)
                } else {
                    // Nếu chỉ có 1 từ, coi như là tên
                    binding.etFirstName.setText(currentUser.fullName)
                }
            }
            
            // Load email nếu có
            if (currentUser.email.isNotEmpty()) {
                binding.etEmail.setText(currentUser.email)
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
            // Nếu file không tồn tại, dùng ảnh mặc định
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
        // Back button - quay về ProfileActivity
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
        
        binding.btnComplete.setOnClickListener {
            val lastName = binding.etLastName.text.toString().trim()
            val firstName = binding.etFirstName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()

            // Validation họ & tên đệm
            val lastNameValidation = ValidationUtils.validateLastName(lastName)
            if (!lastNameValidation.first) {
                binding.etLastName.error = lastNameValidation.second
                binding.etLastName.requestFocus()
                return@setOnClickListener
            }

            // Validation tên
            val firstNameValidation = ValidationUtils.validateFirstName(firstName)
            if (!firstNameValidation.first) {
                binding.etFirstName.error = firstNameValidation.second
                binding.etFirstName.requestFocus()
                return@setOnClickListener
            }

            // Validation email
            val emailValidation = ValidationUtils.validateEmail(email)
            if (!emailValidation.first) {
                binding.etEmail.error = emailValidation.second
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }

            // Ghép họ & tên đệm và tên thành fullName
            val fullName = "$lastName $firstName"

            // Cập nhật user trong UserManager
            val currentUser = userManager.getCurrentUser()
            if (currentUser != null) {
                val updatedUser = currentUser.copy(
                    fullName = fullName,
                    email = email,
                    avatarPath = currentAvatarPath // Lưu đường dẫn ảnh đại diện
                )
                lifecycleScope.launch {
                    val success = userManager.updateUser(
                        currentUser.userId,
                        fullName = fullName,
                        email = email,
                        avatarPath = currentAvatarPath
                    )
                    if (success) {
                        Toast.makeText(this@CompleteProfileActivity, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show()
                        // Chuyển về ProfileActivity để hiển thị thông tin đã cập nhật
                        val intent = Intent(this@CompleteProfileActivity, ProfileActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@CompleteProfileActivity, "Cập nhật thông tin thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }
}

