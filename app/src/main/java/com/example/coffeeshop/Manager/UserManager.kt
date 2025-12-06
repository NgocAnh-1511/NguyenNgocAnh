package com.example.coffeeshop.Manager

import android.content.Context
import android.content.SharedPreferences
import com.example.coffeeshop.Domain.UserModel
import com.example.coffeeshop.Network.ApiClient
import com.example.coffeeshop.Network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserManager(private val context: Context) {
    private val apiService: ApiService = ApiClient.getApiService(context)
    private val prefs: SharedPreferences = context.getSharedPreferences("CoffeeShopPrefs", Context.MODE_PRIVATE)
    
    /**
     * Lưu user vào SharedPreferences (local storage)
     */
    private fun saveUserToLocal(user: UserModel) {
        prefs.edit().apply {
            putString("user_id", user.userId)
            putString("phone_number", user.phoneNumber)
            putString("full_name", user.fullName)
            putString("email", user.email)
            putString("avatar_path", user.avatarPath)
            putLong("created_at", user.createdAt)
            putBoolean("is_admin", user.isAdmin)
            putBoolean("is_logged_in", true)
            apply()
        }
    }
    
    /**
     * Lấy user từ SharedPreferences
     */
    private fun getUserFromLocal(): UserModel? {
        val userId = prefs.getString("user_id", null) ?: return null
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        if (!isLoggedIn) return null
        
        return UserModel(
            userId = userId,
            phoneNumber = prefs.getString("phone_number", "") ?: "",
            fullName = prefs.getString("full_name", "") ?: "",
            email = prefs.getString("email", "") ?: "",
            password = "", // Không lưu password local
            avatarPath = prefs.getString("avatar_path", "") ?: "",
            createdAt = prefs.getLong("created_at", System.currentTimeMillis()),
            isAdmin = prefs.getBoolean("is_admin", false)
        )
    }
    
    /**
     * Đăng nhập qua API
     */
    suspend fun login(phoneNumber: String, password: String): UserModel? = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(
                com.example.coffeeshop.Network.LoginRequest(phoneNumber, password)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                val userResponse = loginResponse.user
                
                // Lưu token
                ApiClient.saveToken(context, loginResponse.access_token)
                
                // Convert và lưu user
                val user = UserModel(
                    userId = userResponse.userId,
                    phoneNumber = userResponse.phoneNumber,
                    fullName = userResponse.fullName ?: "",
                    email = userResponse.email ?: "",
                    password = "", // Không lưu password
                    avatarPath = userResponse.avatarPath ?: "",
                    createdAt = System.currentTimeMillis(), // API không trả về createdAt
                    isAdmin = userResponse.getIsAdmin()
                )
                
                saveUserToLocal(user)
                return@withContext user
            }
            null
        } catch (e: Exception) {
            android.util.Log.e("UserManager", "Login error", e)
            null
        }
    }
    
    /**
     * Đăng ký user mới
     */
    suspend fun registerUser(phoneNumber: String, password: String, fullName: String = "", email: String = ""): Boolean = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("UserManager", "Registering user with phone: $phoneNumber")
            
            // Nếu fullName rỗng, dùng số điện thoại làm tên mặc định
            val finalFullName = if (fullName.isBlank()) phoneNumber else fullName
            // Nếu email rỗng, gửi null thay vì "" để tránh validation error
            val finalEmail = if (email.isBlank()) null else email
            
            android.util.Log.d("UserManager", "Sending register request: phone=$phoneNumber, fullName=$finalFullName, email=$finalEmail")
            
            val request = com.example.coffeeshop.Network.RegisterRequest(phoneNumber, password, finalFullName, finalEmail)
            val response = apiService.register(request)
            
            android.util.Log.d("UserManager", "Register response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                val userResponse = loginResponse.user
                
                // Lưu token
                ApiClient.saveToken(context, loginResponse.access_token)
                
                // Convert và lưu user
                val user = UserModel(
                    userId = userResponse.userId,
                    phoneNumber = userResponse.phoneNumber,
                    fullName = userResponse.fullName ?: "",
                    email = userResponse.email ?: "",
                    password = "",
                    avatarPath = userResponse.avatarPath ?: "",
                    createdAt = System.currentTimeMillis(),
                    isAdmin = userResponse.getIsAdmin()
                )
                
                saveUserToLocal(user)
                android.util.Log.d("UserManager", "Register successful: ${userResponse.userId}")
                return@withContext true
            } else {
                val errorBody = try {
                    response.errorBody()?.string()
                } catch (e: Exception) {
                    "Cannot read error body: ${e.message}"
                }
                android.util.Log.e("UserManager", "Register failed: ${response.code()} - $errorBody")
                
                // Nếu là lỗi 400 (Bad Request), có thể là phone number đã tồn tại
                if (response.code() == 400) {
                    android.util.Log.e("UserManager", "Bad Request - likely phone number already exists or validation failed")
                }
                
                return@withContext false
            }
        } catch (e: java.net.UnknownHostException) {
            android.util.Log.e("UserManager", "Network error: Cannot connect to server. Is backend running?", e)
            return@withContext false
        } catch (e: java.net.ConnectException) {
            android.util.Log.e("UserManager", "Connection error: Cannot connect to server. Is backend running at http://localhost:3000?", e)
            return@withContext false
        } catch (e: Exception) {
            android.util.Log.e("UserManager", "Register error", e)
            e.printStackTrace()
            return@withContext false
        }
    }
    
    /**
     * Lấy user hiện tại (từ local hoặc API)
     */
    fun getCurrentUser(): UserModel? {
        return getUserFromLocal()
    }
    
    /**
     * Lấy user từ API và cập nhật local
     */
    suspend fun refreshCurrentUser(): UserModel? = withContext(Dispatchers.IO) {
        try {
            val token = ApiClient.getToken(context) ?: return@withContext null
            val response = apiService.getProfile("Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                val userResponse = response.body()!!
                val user = UserModel(
                    userId = userResponse.userId,
                    phoneNumber = userResponse.phoneNumber,
                    fullName = userResponse.fullName ?: "",
                    email = userResponse.email ?: "",
                    password = "",
                    avatarPath = userResponse.avatarPath ?: "",
                    createdAt = System.currentTimeMillis(),
                    isAdmin = userResponse.getIsAdmin()
                )
                
                saveUserToLocal(user)
                return@withContext user
            }
            null
        } catch (e: Exception) {
            android.util.Log.e("UserManager", "Refresh user error", e)
            null
        }
    }
    
    /**
     * Cập nhật thông tin user
     */
    suspend fun updateUser(userId: String, fullName: String? = null, email: String? = null, password: String? = null, avatarPath: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = ApiClient.getToken(context) ?: return@withContext false
            android.util.Log.d("UserManager", "Updating user: userId=$userId, hasPassword=${password != null}, hasFullName=${fullName != null}, hasEmail=${email != null}")
            
            val response = apiService.updateUser(
                "Bearer $token",
                userId,
                com.example.coffeeshop.Network.UpdateUserRequest(fullName, email, password, avatarPath)
            )
            
            android.util.Log.d("UserManager", "Update user response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val userResponse = response.body()!!
                android.util.Log.d("UserManager", "Update user successful: ${userResponse.userId}")
                
                val user = UserModel(
                    userId = userResponse.userId,
                    phoneNumber = userResponse.phoneNumber,
                    fullName = userResponse.fullName ?: "",
                    email = userResponse.email ?: "",
                    password = "",
                    avatarPath = userResponse.avatarPath ?: "",
                    createdAt = System.currentTimeMillis(),
                    isAdmin = userResponse.getIsAdmin()
                )
                
                saveUserToLocal(user)
                return@withContext true
            } else {
                val errorBody = try {
                    response.errorBody()?.string()
                } catch (e: Exception) {
                    "Cannot read error body: ${e.message}"
                }
                android.util.Log.e("UserManager", "Update user failed: ${response.code()} - $errorBody")
                return@withContext false
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            android.util.Log.e("UserManager", "Update user JSON parsing error", e)
            e.printStackTrace()
            return@withContext false
        } catch (e: Exception) {
            android.util.Log.e("UserManager", "Update user error", e)
            e.printStackTrace()
            return@withContext false
        }
    }
    
    /**
     * Đăng xuất
     */
    fun logout() {
        prefs.edit().putBoolean("is_logged_in", false).apply()
        ApiClient.clearToken(context)
    }
    
    /**
     * Kiểm tra đã đăng nhập chưa
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false) && ApiClient.getToken(context) != null
    }
    
    /**
     * Lấy user ID
     */
    fun getUserId(): String? {
        return getCurrentUser()?.userId
    }
    
    /**
     * Lấy số điện thoại
     */
    fun getPhoneNumber(): String? {
        return getCurrentUser()?.phoneNumber
    }
    
    /**
     * Kiểm tra có phải admin không
     */
    fun isAdmin(): Boolean {
        return getCurrentUser()?.isAdmin ?: false
    }
    
    /**
     * Lưu token (đã được xử lý trong ApiClient)
     */
    fun saveToken(token: String) {
        ApiClient.saveToken(context, token)
    }
    
    /**
     * Lấy token
     */
    fun getToken(): String? {
        return ApiClient.getToken(context)
    }
    
    /**
     * Xóa token
     */
    fun clearToken() {
        ApiClient.clearToken(context)
    }
    
    /**
     * Kiểm tra số điện thoại đã tồn tại (gọi API)
     */
    suspend fun isPhoneNumberExists(phoneNumber: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = ApiClient.getToken(context) ?: return@withContext false
            val response = apiService.getUsers("Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                val users = response.body()!!
                return@withContext users.any { it.phoneNumber == phoneNumber }
            }
            false
        } catch (e: Exception) {
            android.util.Log.e("UserManager", "Check phone exists error", e)
            false
        }
    }
    
    /**
     * Tạo admin mặc định (không cần nữa vì đã có trong MySQL)
     */
    fun createDefaultAdminIfNotExists() {
        // Không cần làm gì, admin đã có trong MySQL database
    }
}
