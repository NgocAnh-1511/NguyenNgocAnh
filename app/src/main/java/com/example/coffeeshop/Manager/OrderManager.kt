package com.example.coffeeshop.Manager

import android.content.Context
import com.example.coffeeshop.Domain.CartModel
import com.example.coffeeshop.Domain.OrderModel
import com.example.coffeeshop.Network.ApiClient
import com.example.coffeeshop.Network.ApiService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class OrderManager(private val context: Context) {
    private val apiService: ApiService = ApiClient.getApiService(context)
    private val userManager = UserManager(context)
    private val gson = Gson()
    
    private fun getCurrentUserId(): String? {
        return userManager.getUserId()
    }
    
    /**
     * Tạo order mới qua API
     */
    suspend fun createOrder(
        items: MutableList<CartModel>,
        totalPrice: Double,
        deliveryAddress: String = "",
        phoneNumber: String = "",
        customerName: String = "",
        paymentMethod: String = "Tiền mặt"
    ): OrderModel? = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("OrderManager", "=== Creating Order ===")
            android.util.Log.d("OrderManager", "Items count: ${items.size}")
            android.util.Log.d("OrderManager", "Total price: $totalPrice")
            
            val userId = getCurrentUserId()
            if (userId == null) {
                android.util.Log.e("OrderManager", "User ID is null - user not logged in")
                return@withContext null
            }
            android.util.Log.d("OrderManager", "User ID: $userId")
            
            val token = ApiClient.getToken(context)
            if (token == null) {
                android.util.Log.e("OrderManager", "Token is null - user not authenticated")
                android.util.Log.e("OrderManager", "User needs to login again")
                return@withContext null
            }
            android.util.Log.d("OrderManager", "Token exists: ${token.take(20)}...")
            android.util.Log.d("OrderManager", "Full token length: ${token.length}")
            android.util.Log.d("OrderManager", "Token preview: ${token.take(50)}...")
            
            // Convert items to OrderItemRequest
            val orderItems = items.map { cartItem ->
                com.example.coffeeshop.Network.OrderItemRequest(
                    productId = cartItem.item.categoryId.ifEmpty { null }, // Use categoryId as productId or null
                    productName = cartItem.item.title,
                    quantity = cartItem.quantity,
                    price = cartItem.item.price,
                    itemJson = gson.toJson(cartItem.item)
                )
            }
            
            val request = com.example.coffeeshop.Network.CreateOrderRequest(
                userId = userId,
                totalPrice = totalPrice,
                deliveryAddress = deliveryAddress.ifEmpty { null },
                phoneNumber = phoneNumber.ifEmpty { null },
                customerName = customerName.ifEmpty { null },
                paymentMethod = paymentMethod.ifEmpty { null },
                items = orderItems
            )
            
            android.util.Log.d("OrderManager", "Sending create order request to API...")
            android.util.Log.d("OrderManager", "Request body: ${gson.toJson(request)}")
            android.util.Log.d("OrderManager", "Authorization header: Bearer ${token.take(20)}...")
            
            val response = apiService.createOrder("Bearer $token", request)
            
            android.util.Log.d("OrderManager", "Create order response code: ${response.code()}")
            android.util.Log.d("OrderManager", "Response isSuccessful: ${response.isSuccessful}")
            android.util.Log.d("OrderManager", "Response body is null: ${response.body() == null}")
            
            if (response.isSuccessful && response.body() != null) {
                val orderResponse = response.body()!!
                android.util.Log.d("OrderManager", "Order created successfully: ${orderResponse.orderId ?: orderResponse.order_id}")
                return@withContext orderResponse.toOrderModel(items)
            } else {
                val errorBody = try {
                    response.errorBody()?.string()
                } catch (e: Exception) {
                    "Cannot read error body: ${e.message}"
                }
                android.util.Log.e("OrderManager", "Create order failed: ${response.code()} - $errorBody")
                android.util.Log.e("OrderManager", "Response headers: ${response.headers()}")
                return@withContext null
            }
        } catch (e: java.net.UnknownHostException) {
            android.util.Log.e("OrderManager", "Network error: Cannot connect to server. Is backend running?", e)
            return@withContext null
        } catch (e: java.net.ConnectException) {
            android.util.Log.e("OrderManager", "Connection error: Cannot connect to server. Is backend running at http://localhost:3000?", e)
            return@withContext null
        } catch (e: Exception) {
            android.util.Log.e("OrderManager", "Create order error", e)
            e.printStackTrace()
            return@withContext null
        }
    }
    
    /**
     * Lấy tất cả orders của user hiện tại
     */
    suspend fun getAllOrders(): MutableList<OrderModel> = withContext(Dispatchers.IO) {
        try {
            val token = ApiClient.getToken(context) ?: return@withContext mutableListOf()
            val response = apiService.getOrders("Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                val ordersResponse = response.body()!!
                val userId = getCurrentUserId()
                
                // Filter orders by current user (nếu không phải admin)
                val filteredOrders = if (userManager.isAdmin()) {
                    ordersResponse
                } else {
                    ordersResponse.filter { 
                        (it.userId ?: it.user_id) == userId 
                    }
                }
                
                // Convert to OrderModel và fetch items từ order_items table
                return@withContext filteredOrders.map { orderResponse ->
                    // Fetch order items từ API (cần thêm endpoint hoặc include trong response)
                    val items = mutableListOf<CartModel>()
                    // TODO: Fetch order items từ order_items table
                    orderResponse.toOrderModel(items)
                }.toMutableList()
            }
            mutableListOf()
        } catch (e: Exception) {
            android.util.Log.e("OrderManager", "Get all orders error", e)
            mutableListOf()
        }
    }
    
    /**
     * Lấy order theo ID
     */
    suspend fun getOrderById(orderId: String): OrderModel? = withContext(Dispatchers.IO) {
        try {
            val token = ApiClient.getToken(context) ?: return@withContext null
            val response = apiService.getOrder("Bearer $token", orderId)
            
            if (response.isSuccessful && response.body() != null) {
                val orderResponse = response.body()!!
                val items = mutableListOf<CartModel>()
                // TODO: Fetch order items từ order_items table
                return@withContext orderResponse.toOrderModel(items)
            }
            null
        } catch (e: Exception) {
            android.util.Log.e("OrderManager", "Get order by ID error", e)
            null
        }
    }
    
    /**
     * Cập nhật status của order
     */
    suspend fun updateOrderStatus(orderId: String, status: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = ApiClient.getToken(context) ?: return@withContext false
            val response = apiService.updateOrderStatus(
                "Bearer $token",
                orderId,
                com.example.coffeeshop.Network.UpdateStatusRequest(status)
            )
            
            return@withContext response.isSuccessful
        } catch (e: Exception) {
            android.util.Log.e("OrderManager", "Update order status error", e)
            false
        }
    }
    
    /**
     * Lấy tất cả orders (cho admin)
     */
    suspend fun getAllOrdersForAdmin(): MutableList<OrderModel> = withContext(Dispatchers.IO) {
        // Same as getAllOrders, but admin sees all orders
        getAllOrders()
    }
    
    /**
     * Hủy order
     */
    suspend fun cancelOrder(orderId: String): Boolean {
        return updateOrderStatus(orderId, "Cancelled")
    }
    
    /**
     * Xóa order
     */
    suspend fun deleteOrder(orderId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = ApiClient.getToken(context) ?: return@withContext false
            val response = apiService.deleteOrder("Bearer $token", orderId)
            return@withContext response.isSuccessful
        } catch (e: Exception) {
            android.util.Log.e("OrderManager", "Delete order error", e)
            false
        }
    }
}
