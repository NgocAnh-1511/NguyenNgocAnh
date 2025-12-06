package com.example.coffeeshop.Manager

import android.content.Context
import com.example.coffeeshop.Domain.VoucherModel
import com.example.coffeeshop.Network.ApiClient
import com.example.coffeeshop.Network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VoucherManager(private val context: Context) {
    private val apiService: ApiService = ApiClient.getApiService(context)
    
    /**
     * Thêm mã giảm giá mới
     */
    suspend fun addVoucher(voucher: VoucherModel): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = ApiClient.getToken(context) ?: return@withContext false
            val request = com.example.coffeeshop.Network.CreateVoucherRequest(
                code = voucher.code,
                discountPercent = voucher.discountPercent,
                discountAmount = voucher.discountAmount,
                discountType = voucher.discountType.name,
                minOrderAmount = voucher.minOrderAmount,
                maxDiscountAmount = voucher.maxDiscountAmount,
                startDate = voucher.startDate,
                endDate = voucher.endDate,
                usageLimit = voucher.usageLimit,
                description = voucher.description.ifEmpty { null }
            )
            
            val response = apiService.createVoucher("Bearer $token", request)
            return@withContext response.isSuccessful
        } catch (e: Exception) {
            android.util.Log.e("VoucherManager", "Add voucher error", e)
            false
        }
    }
    
    /**
     * Lấy mã giảm giá theo code
     */
    suspend fun getVoucherByCode(code: String): VoucherModel? = withContext(Dispatchers.IO) {
        try {
            val token = ApiClient.getToken(context) ?: return@withContext null
            val response = apiService.getVoucherByCode("Bearer $token", code.uppercase())
            
            if (response.isSuccessful && response.body() != null) {
                return@withContext response.body()!!.toVoucherModel()
            }
            null
        } catch (e: Exception) {
            android.util.Log.e("VoucherManager", "Get voucher by code error", e)
            null
        }
    }
    
    /**
     * Lấy mã giảm giá theo ID
     */
    suspend fun getVoucherById(voucherId: String): VoucherModel? = withContext(Dispatchers.IO) {
        try {
            val token = ApiClient.getToken(context) ?: return@withContext null
            val response = apiService.getVoucher("Bearer $token", voucherId)
            
            if (response.isSuccessful && response.body() != null) {
                return@withContext response.body()!!.toVoucherModel()
            }
            null
        } catch (e: Exception) {
            android.util.Log.e("VoucherManager", "Get voucher by ID error", e)
            null
        }
    }
    
    /**
     * Lấy tất cả mã giảm giá
     */
    suspend fun getAllVouchers(): MutableList<VoucherModel> = withContext(Dispatchers.IO) {
        try {
            val token = ApiClient.getToken(context) ?: return@withContext mutableListOf()
            val response = apiService.getVouchers("Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                val vouchers = response.body()!!.map { it.toVoucherModel() }
                return@withContext vouchers.toMutableList()
            }
            mutableListOf()
        } catch (e: Exception) {
            android.util.Log.e("VoucherManager", "Get all vouchers error", e)
            mutableListOf()
        }
    }
    
    /**
     * Lấy tất cả mã giảm giá đang hoạt động
     */
    suspend fun getActiveVouchers(): MutableList<VoucherModel> = withContext(Dispatchers.IO) {
        try {
            val allVouchers = getAllVouchers()
            val currentTime = System.currentTimeMillis()
            return@withContext allVouchers.filter { it.isValid(currentTime) }.toMutableList()
        } catch (e: Exception) {
            android.util.Log.e("VoucherManager", "Get active vouchers error", e)
            mutableListOf()
        }
    }
    
    /**
     * Kiểm tra mã giảm giá có hợp lệ không
     */
    suspend fun validateVoucher(code: String, orderAmount: Double = 0.0): VoucherModel? = withContext(Dispatchers.IO) {
        try {
            val voucher = getVoucherByCode(code) ?: return@withContext null
            
            if (!voucher.isValid()) return@withContext null
            if (orderAmount > 0 && orderAmount < voucher.minOrderAmount) return@withContext null
            
            return@withContext voucher
        } catch (e: Exception) {
            android.util.Log.e("VoucherManager", "Validate voucher error", e)
            null
        }
    }
    
    /**
     * Tăng số lần sử dụng mã giảm giá
     */
    suspend fun incrementUsageCount(voucherId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val voucher = getVoucherById(voucherId) ?: return@withContext false
            val token = ApiClient.getToken(context) ?: return@withContext false
            
            val request = com.example.coffeeshop.Network.UpdateVoucherRequest(
                usedCount = voucher.usedCount + 1
            )
            
            val response = apiService.updateVoucher("Bearer $token", voucherId, request)
            return@withContext response.isSuccessful
        } catch (e: Exception) {
            android.util.Log.e("VoucherManager", "Increment usage count error", e)
            false
        }
    }
    
    /**
     * Cập nhật mã giảm giá
     */
    suspend fun updateVoucher(voucher: VoucherModel): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = ApiClient.getToken(context) ?: return@withContext false
            val request = com.example.coffeeshop.Network.UpdateVoucherRequest(
                code = voucher.code,
                discountPercent = voucher.discountPercent,
                discountAmount = voucher.discountAmount,
                discountType = voucher.discountType.name,
                minOrderAmount = voucher.minOrderAmount,
                maxDiscountAmount = voucher.maxDiscountAmount,
                startDate = voucher.startDate,
                endDate = voucher.endDate,
                usageLimit = voucher.usageLimit,
                usedCount = voucher.usedCount,
                isActive = voucher.isActive,
                description = voucher.description.ifEmpty { null }
            )
            
            val response = apiService.updateVoucher("Bearer $token", voucher.voucherId, request)
            return@withContext response.isSuccessful
        } catch (e: Exception) {
            android.util.Log.e("VoucherManager", "Update voucher error", e)
            false
        }
    }
    
    /**
     * Xóa mã giảm giá
     */
    suspend fun deleteVoucher(voucherId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = ApiClient.getToken(context) ?: return@withContext false
            val response = apiService.deleteVoucher("Bearer $token", voucherId)
            return@withContext response.isSuccessful
        } catch (e: Exception) {
            android.util.Log.e("VoucherManager", "Delete voucher error", e)
            false
        }
    }
}
