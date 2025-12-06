package com.example.coffeeshop.Network

import com.example.coffeeshop.Domain.OrderModel
import com.example.coffeeshop.Domain.UserModel
import com.example.coffeeshop.Domain.VoucherModel
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth endpoints
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>
    
    @GET("auth/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<UserResponse>
    
    // Users endpoints
    @GET("users")
    suspend fun getUsers(@Header("Authorization") token: String): Response<List<UserResponse>>
    
    @GET("users/{userId}")
    suspend fun getUser(@Header("Authorization") token: String, @Path("userId") userId: String): Response<UserResponse>
    
    @PATCH("users/{userId}")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Body request: UpdateUserRequest
    ): Response<UserResponse>
    
    // Orders endpoints
    @GET("orders")
    suspend fun getOrders(@Header("Authorization") token: String): Response<List<OrderResponse>>
    
    @GET("orders/{orderId}")
    suspend fun getOrder(@Header("Authorization") token: String, @Path("orderId") orderId: String): Response<OrderResponse>
    
    @POST("orders")
    suspend fun createOrder(@Header("Authorization") token: String, @Body request: CreateOrderRequest): Response<OrderResponse>
    
    @PATCH("orders/{orderId}")
    suspend fun updateOrder(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: String,
        @Body request: UpdateOrderRequest
    ): Response<OrderResponse>
    
    @PATCH("orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: String,
        @Body request: UpdateStatusRequest
    ): Response<OrderResponse>
    
    @DELETE("orders/{orderId}")
    suspend fun deleteOrder(@Header("Authorization") token: String, @Path("orderId") orderId: String): Response<Unit>
    
    // Vouchers endpoints
    @GET("vouchers")
    suspend fun getVouchers(@Header("Authorization") token: String): Response<List<VoucherResponse>>
    
    @GET("vouchers/{voucherId}")
    suspend fun getVoucher(@Header("Authorization") token: String, @Path("voucherId") voucherId: String): Response<VoucherResponse>
    
    @GET("vouchers/code/{code}")
    suspend fun getVoucherByCode(@Header("Authorization") token: String, @Path("code") code: String): Response<VoucherResponse>
    
    @POST("vouchers")
    suspend fun createVoucher(@Header("Authorization") token: String, @Body request: CreateVoucherRequest): Response<VoucherResponse>
    
    @PATCH("vouchers/{voucherId}")
    suspend fun updateVoucher(
        @Header("Authorization") token: String,
        @Path("voucherId") voucherId: String,
        @Body request: UpdateVoucherRequest
    ): Response<VoucherResponse>
    
    @DELETE("vouchers/{voucherId}")
    suspend fun deleteVoucher(@Header("Authorization") token: String, @Path("voucherId") voucherId: String): Response<Unit>
    
    // Public Products endpoints (không cần auth)
    @GET("public/products")
    suspend fun getProducts(@Query("categoryId") categoryId: Int? = null): Response<List<ProductResponse>>
    
    @GET("public/products/{id}")
    suspend fun getProduct(@Path("id") id: Int): Response<ProductResponse>
    
    // Public Categories endpoints (không cần auth)
    @GET("public/categories")
    suspend fun getCategories(): Response<List<CategoryResponse>>
    
    @GET("public/categories/{id}")
    suspend fun getCategory(@Path("id") id: Int): Response<CategoryResponse>
}

// Request DTOs
data class LoginRequest(
    val phoneNumber: String,
    val password: String
)

data class RegisterRequest(
    val phoneNumber: String,
    val password: String,
    val fullName: String? = null,
    val email: String? = null
)

data class UpdateUserRequest(
    val fullName: String? = null,
    val email: String? = null,
    val password: String? = null,
    val avatarPath: String? = null
)

data class CreateOrderRequest(
    val userId: String,
    val totalPrice: Double,
    val deliveryAddress: String? = null,
    val phoneNumber: String? = null,
    val customerName: String? = null,
    val paymentMethod: String? = null,
    val items: List<OrderItemRequest>
)

data class OrderItemRequest(
    val productId: String? = null,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val itemJson: String? = null
)

data class UpdateOrderRequest(
    val status: String? = null,
    val deliveryAddress: String? = null,
    val phoneNumber: String? = null,
    val customerName: String? = null,
    val paymentMethod: String? = null
)

data class UpdateStatusRequest(
    val status: String
)

data class CreateVoucherRequest(
    val code: String,
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0,
    val discountType: String = "PERCENT",
    val minOrderAmount: Double = 0.0,
    val maxDiscountAmount: Double = 0.0,
    val startDate: Long,
    val endDate: Long,
    val usageLimit: Int = 0,
    val description: String? = null
)

data class UpdateVoucherRequest(
    val code: String? = null,
    val discountPercent: Double? = null,
    val discountAmount: Double? = null,
    val discountType: String? = null,
    val minOrderAmount: Double? = null,
    val maxDiscountAmount: Double? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val usageLimit: Int? = null,
    val usedCount: Int? = null,
    val isActive: Boolean? = null,
    val description: String? = null
)

// Response DTOs
data class LoginResponse(
    val access_token: String,
    val user: UserResponse
)

data class UserResponse(
    val userId: String,
    val phoneNumber: String,
    val fullName: String? = null,
    val email: String? = null,
    val avatarPath: String? = null,
    // Backend trả về isAdmin dưới dạng number (0/1), không phải boolean
    // Sử dụng Int? để nhận number, sau đó convert sang boolean bằng getIsAdmin()
    val isAdmin: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    /**
     * Convert isAdmin từ Int (0/1) sang Boolean
     */
    fun getIsAdmin(): Boolean {
        return (isAdmin ?: 0) != 0
    }
}

data class OrderResponse(
    val orderId: String? = null,
    val order_id: String? = null, // Support both formats
    val userId: String? = null,
    val user_id: String? = null,
    val totalPrice: Double? = null,
    val total_price: Double? = null,
    val orderDate: Long? = null,
    val order_date: Long? = null,
    val status: String,
    val deliveryAddress: String? = null,
    val delivery_address: String? = null,
    val phoneNumber: String? = null,
    val phone_number: String? = null,
    val customerName: String? = null,
    val customer_name: String? = null,
    val paymentMethod: String? = null,
    val payment_method: String? = null,
    val items: List<OrderItemResponse>? = null
) {
    fun toOrderModel(itemsList: MutableList<com.example.coffeeshop.Domain.CartModel> = mutableListOf()): OrderModel {
        return OrderModel(
            orderId = orderId ?: order_id ?: "",
            items = itemsList,
            totalPrice = totalPrice ?: total_price ?: 0.0,
            orderDate = orderDate ?: order_date ?: System.currentTimeMillis(),
            status = status,
            deliveryAddress = deliveryAddress ?: delivery_address ?: "",
            phoneNumber = phoneNumber ?: phone_number ?: "",
            customerName = customerName ?: customer_name ?: "",
            paymentMethod = paymentMethod ?: payment_method ?: "Tiền mặt"
        )
    }
}

data class OrderItemResponse(
    val id: Int? = null,
    val orderId: String? = null,
    val order_id: String? = null,
    val productId: String? = null,
    val product_id: String? = null,
    val productName: String? = null,
    val product_name: String? = null,
    val quantity: Int,
    val price: Double,
    val itemJson: String? = null,
    val item_json: String? = null
)

data class VoucherResponse(
    val voucherId: String? = null,
    val voucher_id: String? = null,
    val code: String,
    val discountPercent: Double? = null,
    val discount_percent: Double? = null,
    val discountAmount: Double? = null,
    val discount_amount: Double? = null,
    val discountType: String? = null,
    val discount_type: String? = null,
    val minOrderAmount: Double? = null,
    val min_order_amount: Double? = null,
    val maxDiscountAmount: Double? = null,
    val max_discount_amount: Double? = null,
    val startDate: Long? = null,
    val start_date: Long? = null,
    val endDate: Long? = null,
    val end_date: Long? = null,
    val usageLimit: Int? = null,
    val usage_limit: Int? = null,
    val usedCount: Int? = null,
    val used_count: Int? = null,
    // Backend trả về isActive dưới dạng number (0/1), không phải boolean
    // Sử dụng Int? để nhận number, sau đó convert sang boolean trong toVoucherModel()
    val isActive: Int? = null,
    val is_active: Int? = null,
    val description: String? = null
) {
    fun toVoucherModel(): VoucherModel {
        return VoucherModel(
            voucherId = voucherId ?: voucher_id ?: "",
            code = code,
            discountPercent = discountPercent ?: discount_percent ?: 0.0,
            discountAmount = discountAmount ?: discount_amount ?: 0.0,
            discountType = when (discountType ?: discount_type) {
                "AMOUNT" -> VoucherModel.DiscountType.AMOUNT
                else -> VoucherModel.DiscountType.PERCENT
            },
            minOrderAmount = minOrderAmount ?: min_order_amount ?: 0.0,
            maxDiscountAmount = maxDiscountAmount ?: max_discount_amount ?: 0.0,
            startDate = startDate ?: start_date ?: 0L,
            endDate = endDate ?: end_date ?: 0L,
            usageLimit = usageLimit ?: usage_limit ?: 0,
            usedCount = usedCount ?: used_count ?: 0,
            // Convert number (0/1) sang boolean: 1 = true, 0 = false
            isActive = (isActive ?: is_active ?: 1) != 0,
            description = description ?: ""
        )
    }
}

// Product Response DTOs
data class ProductResponse(
    val id: Int,
    val name: String,
    val description: String? = null,
    val price: Double,
    val originalPrice: Double? = null,
    val imageUrl: String? = null,
    val stock: Int = 0,
    val isActive: Boolean = true,
    val categoryId: Int? = null,
    val category: CategoryResponse? = null
) {
    fun toItemsModel(): com.example.coffeeshop.Domain.ItemsModel {
        return com.example.coffeeshop.Domain.ItemsModel(
            title = name,
            description = description ?: "",
            picUrl = if (imageUrl != null) arrayListOf(imageUrl) else arrayListOf(),
            price = price,
            rating = 0.0,
            numberInCart = 0,
            extra = "",
            categoryId = categoryId?.toString() ?: ""
        )
    }
}

// Category Response DTOs
data class CategoryResponse(
    val id: Int,
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null
) {
    fun toCategoryModel(): com.example.coffeeshop.Domain.CategoryModel {
        return com.example.coffeeshop.Domain.CategoryModel(
            id = id,
            title = name
        )
    }
}

