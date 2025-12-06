package com.example.coffeeshop.Manager

import android.content.Context
import android.util.Log
import com.example.coffeeshop.Database.DatabaseHelper
import com.example.coffeeshop.Domain.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

/**
 * FirebaseSyncManager - Quản lý đồng bộ dữ liệu giữa SQLite và Firebase
 * Sử dụng Coroutines để tránh ANR (Application Not Responding)
 */
class FirebaseSyncManager(private val context: Context) {
    private val firebaseDatabase: FirebaseDatabase by lazy {
        try {
            FirebaseDatabase.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing FirebaseDatabase", e)
            throw e
        }
    }
    
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val gson = Gson()
    
    // Lưu trữ các listeners để có thể remove sau
    private var userListener: ValueEventListener? = null
    private var cartListener: ValueEventListener? = null
    private var ordersListener: ValueEventListener? = null
    private var addressesListener: ValueEventListener? = null
    private var wishlistListener: ValueEventListener? = null
    private var currentUserId: String? = null
    
    companion object {
        private const val TAG = "FirebaseSyncManager"
        private const val SYNC_DELAY_MS = 300L // Delay giữa các lần sync để tránh quá tải
    }
    
    /**
     * Đồng bộ tất cả dữ liệu của user lên Firebase (chạy trên background thread)
     */
    fun syncAllDataToFirebaseAsync(userId: String) {
        syncScope.launch {
            try {
                Log.d(TAG, "Starting sync all data to Firebase for user: $userId")
                
                val userManager = UserManager(context)
                val user = userManager.getCurrentUser()
                if (user != null && user.userId == userId) {
                    syncUserToFirebase(user)
                    delay(SYNC_DELAY_MS)
                }
                
                syncCartToFirebase(userId)
                delay(SYNC_DELAY_MS)
                syncOrdersToFirebase(userId)
                delay(SYNC_DELAY_MS)
                syncAddressesToFirebase(userId)
                delay(SYNC_DELAY_MS)
                syncWishlistToFirebase(userId)
                
                Log.d(TAG, "Sync all data to Firebase completed for user: $userId")
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing all data to Firebase", e)
            }
        }
    }
    
    /**
     * Đồng bộ tất cả dữ liệu từ Firebase về SQLite (chạy trên background thread)
     */
    fun syncAllDataFromFirebaseAsync(userId: String, onComplete: (Boolean) -> Unit = {}) {
        syncScope.launch {
            try {
                Log.d(TAG, "Starting sync all data from Firebase for user: $userId")
                
                val userSynced = syncUserFromFirebase(userId)
                delay(SYNC_DELAY_MS)
                val cartSynced = syncCartFromFirebase(userId)
                delay(SYNC_DELAY_MS)
                val ordersSynced = syncOrdersFromFirebase(userId)
                delay(SYNC_DELAY_MS)
                val addressesSynced = syncAddressesFromFirebase(userId)
                delay(SYNC_DELAY_MS)
                val wishlistSynced = syncWishlistFromFirebase(userId)
                
                val success = userSynced || cartSynced || ordersSynced || addressesSynced || wishlistSynced
                Log.d(TAG, "Sync all data from Firebase completed: success=$success")
                
                withContext(Dispatchers.Main) {
                    onComplete(success)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing all data from Firebase", e)
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }
    
    /**
     * Đồng bộ User lên Firebase
     */
    private suspend fun syncUserToFirebase(user: UserModel): Boolean = withContext(Dispatchers.IO) {
        try {
            val userRef = firebaseDatabase.getReference("users").child(user.userId)
            val userMap = mapOf(
                "userId" to user.userId,
                "phoneNumber" to user.phoneNumber,
                "fullName" to user.fullName,
                "email" to user.email,
                "avatarPath" to user.avatarPath,
                "createdAt" to user.createdAt,
                "password" to user.password,
                "isAdmin" to user.isAdmin
            )
            userRef.setValue(userMap).await()
            Log.d(TAG, "User synced to Firebase: ${user.userId}")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing user to Firebase", e)
            return@withContext false
        }
    }
    
    /**
     * Đồng bộ User từ Firebase về SQLite
     */
    private suspend fun syncUserFromFirebase(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userRef = firebaseDatabase.getReference("users").child(userId)
            val snapshot = userRef.get().await()
            
            if (snapshot.exists()) {
                val userIdValue = getStringValue(snapshot, "userId")
                val phoneNumber = getStringValue(snapshot, "phoneNumber")
                val fullName = getStringValue(snapshot, "fullName")
                val email = getStringValue(snapshot, "email")
                val password = getStringValue(snapshot, "password")
                val avatarPath = getStringValue(snapshot, "avatarPath")
                val createdAt = getLongValue(snapshot, "createdAt", System.currentTimeMillis())
                val isAdmin = getBooleanValue(snapshot, "isAdmin")
                
                val user = UserModel(
                    userId = userIdValue,
                    phoneNumber = phoneNumber,
                    fullName = fullName,
                    email = email,
                    password = password,
                    avatarPath = avatarPath,
                    createdAt = createdAt,
                    isAdmin = isAdmin
                )
                
                // User đã được lưu qua API, không cần saveUser nữa
                // val userManager = UserManager(context)
                // userManager.saveUser(user) // Removed - using API now
                Log.d(TAG, "User synced from Firebase: $userId")
                return@withContext true
            }
            return@withContext false
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing user from Firebase", e)
            return@withContext false
        }
    }
    
    /**
     * Đồng bộ Cart lên Firebase
     */
    private suspend fun syncCartToFirebase(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val cartManager = CartManager(context)
            val cartList = cartManager.getCartList()
            val cartRef = firebaseDatabase.getReference("carts").child(userId)
            
            // Xóa cart cũ trên Firebase
            cartRef.removeValue().await()
            
            // Thêm cart mới
            cartList.forEach { cartItem ->
                val itemKey = cartItem.item.title.replace(".", "_")
                    .replace("#", "_").replace("$", "_")
                    .replace("[", "_").replace("]", "_")
                val cartMap = mapOf(
                    "item" to gson.toJson(cartItem.item),
                    "quantity" to cartItem.quantity
                )
                cartRef.child(itemKey).setValue(cartMap).await()
            }
            
            Log.d(TAG, "Cart synced to Firebase: ${cartList.size} items")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing cart to Firebase", e)
            return@withContext false
        }
    }
    
    /**
     * Đồng bộ Cart từ Firebase về SQLite
     */
    private suspend fun syncCartFromFirebase(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val cartRef = firebaseDatabase.getReference("carts").child(userId)
            val snapshot = cartRef.get().await()
            
            if (snapshot.exists()) {
                val cartManager = CartManager(context)
                cartManager.clearCart()
                
                snapshot.children.forEach { child ->
                    try {
                        val itemJson = child.child("item").getValue(String::class.java) ?: return@forEach
                        val quantity = child.child("quantity").getValue(Int::class.java) ?: 1
                        val item = gson.fromJson(itemJson, ItemsModel::class.java)
                        repeat(quantity) {
                            cartManager.addToCart(item)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing cart item", e)
                    }
                }
                
                Log.d(TAG, "Cart synced from Firebase")
                return@withContext true
            }
            return@withContext false
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing cart from Firebase", e)
            return@withContext false
        }
    }
    
    /**
     * Đồng bộ Orders lên Firebase
     */
    private suspend fun syncOrdersToFirebase(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val orderManager = OrderManager(context)
            val orders = orderManager.getAllOrders()
            val ordersRef = firebaseDatabase.getReference("orders").child(userId)
            
            orders.forEach { order ->
                val orderMap = mapOf(
                    "orderId" to order.orderId,
                    "items" to gson.toJson(order.items),
                    "totalPrice" to order.totalPrice,
                    "orderDate" to order.orderDate,
                    "status" to order.status,
                    "deliveryAddress" to order.deliveryAddress,
                    "phoneNumber" to order.phoneNumber,
                    "customerName" to order.customerName,
                    "paymentMethod" to order.paymentMethod
                )
                ordersRef.child(order.orderId).setValue(orderMap).await()
            }
            
            Log.d(TAG, "Orders synced to Firebase: ${orders.size} orders")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing orders to Firebase", e)
            return@withContext false
        }
    }
    
    /**
     * Đồng bộ Orders từ Firebase về SQLite
     */
    private suspend fun syncOrdersFromFirebase(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val ordersRef = firebaseDatabase.getReference("orders").child(userId)
            val snapshot = ordersRef.get().await()
            
            if (snapshot.exists()) {
                val orderManager = OrderManager(context)
                val db = DatabaseHelper(context).writableDatabase
                
                snapshot.children.forEach { child ->
                    try {
                        val orderId = child.child("orderId").getValue(String::class.java) ?: return@forEach
                        val itemsJson = child.child("items").getValue(String::class.java) ?: ""
                        val totalPrice = child.child("totalPrice").getValue(Double::class.java) ?: 0.0
                        val orderDate = child.child("orderDate").getValue(Long::class.java) ?: System.currentTimeMillis()
                        val status = child.child("status").getValue(String::class.java) ?: "Pending"
                        val deliveryAddress = child.child("deliveryAddress").getValue(String::class.java) ?: ""
                        val phoneNumber = child.child("phoneNumber").getValue(String::class.java) ?: ""
                        val customerName = child.child("customerName").getValue(String::class.java) ?: ""
                        val paymentMethod = child.child("paymentMethod").getValue(String::class.java) ?: "Tiền mặt"
                        
                        val values = android.content.ContentValues().apply {
                            put(DatabaseHelper.COL_ORDER_ID, orderId)
                            put(DatabaseHelper.COL_ORDER_USER_ID, userId)
                            put(DatabaseHelper.COL_ITEMS_JSON, itemsJson)
                            put(DatabaseHelper.COL_TOTAL_PRICE, totalPrice)
                            put(DatabaseHelper.COL_ORDER_DATE, orderDate)
                            put(DatabaseHelper.COL_STATUS, status)
                            put(DatabaseHelper.COL_DELIVERY_ADDRESS, deliveryAddress)
                            put(DatabaseHelper.COL_ORDER_PHONE, phoneNumber)
                            put(DatabaseHelper.COL_CUSTOMER_NAME, customerName)
                            put(DatabaseHelper.COL_PAYMENT_METHOD, paymentMethod)
                        }
                        
                        // Kiểm tra xem order đã tồn tại chưa
                        val existingOrder = orderManager.getOrderById(orderId)
                        if (existingOrder == null) {
                            // Thêm mới
                            db.insert(DatabaseHelper.TABLE_ORDERS, null, values)
                        } else {
                            // Cập nhật status và các thông tin khác
                            db.update(
                                DatabaseHelper.TABLE_ORDERS,
                                values,
                                "${DatabaseHelper.COL_ORDER_ID} = ?",
                                arrayOf(orderId)
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing order", e)
                    }
                }
                
                db.close()
                Log.d(TAG, "Orders synced from Firebase")
                return@withContext true
            }
            return@withContext false
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing orders from Firebase", e)
            return@withContext false
        }
    }
    
    /**
     * Đồng bộ Addresses lên Firebase
     */
    private suspend fun syncAddressesToFirebase(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val addressManager = AddressManager(context)
            val addresses = addressManager.getAllAddresses()
            val addressesRef = firebaseDatabase.getReference("addresses").child(userId)
            
            addressesRef.removeValue().await()
            
            addresses.forEach { address ->
                val addressMap = mapOf(
                    "id" to address.id,
                    "name" to address.name,
                    "phone" to address.phone,
                    "address" to address.address,
                    "isDefault" to address.isDefault
                )
                addressesRef.child(address.id).setValue(addressMap).await()
            }
            
            Log.d(TAG, "Addresses synced to Firebase: ${addresses.size} addresses")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing addresses to Firebase", e)
            return@withContext false
        }
    }
    
    /**
     * Đồng bộ Addresses từ Firebase về SQLite
     */
    private suspend fun syncAddressesFromFirebase(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val addressesRef = firebaseDatabase.getReference("addresses").child(userId)
            val snapshot = addressesRef.get().await()
            
            if (snapshot.exists()) {
                val addressManager = AddressManager(context)
                
                snapshot.children.forEach { child ->
                    try {
                        val addressId = child.child("id").getValue(String::class.java) ?: return@forEach
                        val name = child.child("name").getValue(String::class.java) ?: ""
                        val phone = child.child("phone").getValue(String::class.java) ?: ""
                        val address = child.child("address").getValue(String::class.java) ?: ""
                        val isDefault = child.child("isDefault").getValue(Boolean::class.java) ?: false
                        
                        val addressModel = AddressModel(
                            id = addressId,
                            name = name,
                            phone = phone,
                            address = address,
                            isDefault = isDefault
                        )
                        
                        addressManager.saveAddress(addressModel)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing address", e)
                    }
                }
                
                Log.d(TAG, "Addresses synced from Firebase")
                return@withContext true
            }
            return@withContext false
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing addresses from Firebase", e)
            return@withContext false
        }
    }
    
    /**
     * Đồng bộ Wishlist lên Firebase
     */
    private suspend fun syncWishlistToFirebase(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val wishlistManager = WishlistManager(context)
            val wishlist = wishlistManager.getWishlist()
            val wishlistRef = firebaseDatabase.getReference("wishlist").child(userId)
            
            wishlistRef.removeValue().await()
            
            wishlist.forEach { item ->
                val itemKey = item.title.replace(".", "_")
                    .replace("#", "_").replace("$", "_")
                    .replace("[", "_").replace("]", "_")
                val itemMap = mapOf(
                    "item" to gson.toJson(item)
                )
                wishlistRef.child(itemKey).setValue(itemMap).await()
            }
            
            Log.d(TAG, "Wishlist synced to Firebase: ${wishlist.size} items")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing wishlist to Firebase", e)
            return@withContext false
        }
    }
    
    /**
     * Đồng bộ Wishlist từ Firebase về SQLite
     */
    private suspend fun syncWishlistFromFirebase(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val wishlistRef = firebaseDatabase.getReference("wishlist").child(userId)
            val snapshot = wishlistRef.get().await()
            
            if (snapshot.exists()) {
                val wishlistManager = WishlistManager(context)
                wishlistManager.clearWishlist()
                
                snapshot.children.forEach { child ->
                    try {
                        val itemJson = child.child("item").getValue(String::class.java) ?: return@forEach
                        val item = gson.fromJson(itemJson, ItemsModel::class.java)
                        wishlistManager.addToWishlist(item)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing wishlist item", e)
                    }
                }
                
                Log.d(TAG, "Wishlist synced from Firebase")
                return@withContext true
            }
            return@withContext false
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing wishlist from Firebase", e)
            return@withContext false
        }
    }
    
    /**
     * Tìm user theo phone number trên Firebase
     */
    fun findUserByPhoneNumberAsync(phoneNumber: String, onComplete: (UserModel?) -> Unit) {
        syncScope.launch {
            try {
                if (phoneNumber.isEmpty()) {
                    Log.w(TAG, "Empty phone number provided")
                    withContext(Dispatchers.Main) {
                        onComplete(null)
                    }
                    return@launch
                }
                
                val trimmedPhone = phoneNumber.trim()
                Log.d(TAG, "=== Searching for user with phone: '$trimmedPhone' ===")
                
                val usersRef = firebaseDatabase.getReference("users")
                
                // Fallback: Lấy tất cả users và tìm trong code (đảm bảo hoạt động)
                try {
                    Log.d(TAG, "Getting all users from Firebase...")
                    val allUsersSnapshot = usersRef.get().await()
                    val userCount = allUsersSnapshot.children.count()
                    Log.d(TAG, "Total users in Firebase: $userCount")
                    
                    if (userCount == 0) {
                        Log.w(TAG, "No users found in Firebase")
                        withContext(Dispatchers.Main) {
                            onComplete(null)
                        }
                        return@launch
                    }
                    
                    var foundUser: UserModel? = null
                    for (child in allUsersSnapshot.children) {
                        try {
                            val phone = getStringValue(child, "phoneNumber")
                            val phoneTrimmed = phone.trim()
                            
                            Log.d(TAG, "Checking user - phone in DB: '$phone', trimmed: '$phoneTrimmed', searching for: '$trimmedPhone'")
                            
                            if (phoneTrimmed == trimmedPhone) {
                                val userId = getStringValue(child, "userId")
                                val fullName = getStringValue(child, "fullName")
                                val email = getStringValue(child, "email")
                                val password = getStringValue(child, "password")
                                val avatarPath = getStringValue(child, "avatarPath")
                                val isAdmin = getBooleanValue(child, "isAdmin")
                                val createdAt = getLongValue(child, "createdAt", System.currentTimeMillis())
                                
                                Log.d(TAG, "✓ Found matching user!")
                                Log.d(TAG, "  - userId: $userId")
                                Log.d(TAG, "  - phoneNumber: '$phone'")
                                Log.d(TAG, "  - fullName: '$fullName'")
                                Log.d(TAG, "  - password: '${password.take(3)}...' (length: ${password.length})")
                                Log.d(TAG, "  - isAdmin: $isAdmin")
                                
                                foundUser = UserModel(
                                    userId = userId,
                                    phoneNumber = phone,
                                    fullName = fullName,
                                    email = email,
                                    password = password,
                                    avatarPath = avatarPath,
                                    createdAt = createdAt,
                                    isAdmin = isAdmin
                                )
                                break
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing user data", e)
                            continue
                        }
                    }
                    
                    if (foundUser != null) {
                        Log.d(TAG, "=== Successfully found user in Firebase ===")
                        withContext(Dispatchers.Main) {
                            onComplete(foundUser)
                        }
                    } else {
                        Log.w(TAG, "=== User not found in Firebase: '$trimmedPhone' ===")
                        withContext(Dispatchers.Main) {
                            onComplete(null)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting users from Firebase: ${e.message}", e)
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        onComplete(null)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error finding user in Firebase: ${e.message}", e)
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onComplete(null)
                }
            }
        }
    }
    
    /**
     * Đồng bộ User lên Firebase (public method)
     */
    fun syncUserToFirebaseAsync(user: UserModel) {
        syncScope.launch {
            syncUserToFirebase(user)
        }
    }
    
    /**
     * Bắt đầu lắng nghe thay đổi realtime từ Firebase và đồng bộ về SQLite
     * Chỉ nên gọi khi user đã đăng nhập
     */
    fun startRealtimeSync(userId: String) {
        if (currentUserId == userId) {
            // Đã đang lắng nghe cho user này rồi
            return
        }
        
        // Dừng listeners cũ nếu có
        stopRealtimeSync()
        
        currentUserId = userId
        Log.d(TAG, "Starting realtime sync for user: $userId")
        
        // Lắng nghe thay đổi User
        startUserRealtimeSync(userId)
        
        // Lắng nghe thay đổi Cart
        startCartRealtimeSync(userId)
        
        // Lắng nghe thay đổi Orders
        startOrdersRealtimeSync(userId)
        
        // Lắng nghe thay đổi Addresses
        startAddressesRealtimeSync(userId)
        
        // Lắng nghe thay đổi Wishlist
        startWishlistRealtimeSync(userId)
    }
    
    /**
     * Dừng lắng nghe thay đổi realtime từ Firebase
     */
    fun stopRealtimeSync() {
        val userId = currentUserId
        Log.d(TAG, "Stopping realtime sync for user: $userId")
        
        // Remove User listener
        userListener?.let {
            try {
                if (userId != null) {
                    firebaseDatabase.getReference("users").child(userId).removeEventListener(it)
                } else {
                    firebaseDatabase.getReference("users").removeEventListener(it)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing user listener", e)
            }
        }
        userListener = null
        
        // Remove Cart listener
        cartListener?.let {
            try {
                if (userId != null) {
                    firebaseDatabase.getReference("carts").child(userId).removeEventListener(it)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing cart listener", e)
            }
        }
        cartListener = null
        
        // Remove Orders listener
        ordersListener?.let {
            try {
                if (userId != null) {
                    firebaseDatabase.getReference("orders").child(userId).removeEventListener(it)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing orders listener", e)
            }
        }
        ordersListener = null
        
        // Remove Addresses listener
        addressesListener?.let {
            try {
                if (userId != null) {
                    firebaseDatabase.getReference("addresses").child(userId).removeEventListener(it)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing addresses listener", e)
            }
        }
        addressesListener = null
        
        // Remove Wishlist listener
        wishlistListener?.let {
            try {
                if (userId != null) {
                    firebaseDatabase.getReference("wishlist").child(userId).removeEventListener(it)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing wishlist listener", e)
            }
        }
        wishlistListener = null
        
        currentUserId = null
    }
    
    /**
     * Lắng nghe thay đổi User từ Firebase (realtime)
     */
    private fun startUserRealtimeSync(userId: String) {
        val userRef = firebaseDatabase.getReference("users").child(userId)
        
        userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Chạy trên background thread để tránh ANR
                syncScope.launch {
                    try {
                        if (snapshot.exists()) {
                            syncUserFromFirebase(userId)
                            Log.d(TAG, "User data changed on Firebase, synced to SQLite")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing user from Firebase (realtime)", e)
                    }
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "User listener cancelled: ${error.message}")
            }
        }
        
        userRef.addValueEventListener(userListener!!)
    }
    
    /**
     * Lắng nghe thay đổi Cart từ Firebase (realtime)
     */
    private fun startCartRealtimeSync(userId: String) {
        val cartRef = firebaseDatabase.getReference("carts").child(userId)
        
        cartListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Chạy trên background thread để tránh ANR
                syncScope.launch {
                    try {
                        syncCartFromFirebase(userId)
                        Log.d(TAG, "Cart data changed on Firebase, synced to SQLite")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing cart from Firebase (realtime)", e)
                    }
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Cart listener cancelled: ${error.message}")
            }
        }
        
        cartRef.addValueEventListener(cartListener!!)
    }
    
    /**
     * Lắng nghe thay đổi Orders từ Firebase (realtime)
     */
    private fun startOrdersRealtimeSync(userId: String) {
        val ordersRef = firebaseDatabase.getReference("orders").child(userId)
        
        ordersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Chạy trên background thread để tránh ANR
                syncScope.launch {
                    try {
                        syncOrdersFromFirebase(userId)
                        Log.d(TAG, "Orders data changed on Firebase, synced to SQLite")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing orders from Firebase (realtime)", e)
                    }
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Orders listener cancelled: ${error.message}")
            }
        }
        
        ordersRef.addValueEventListener(ordersListener!!)
    }
    
    /**
     * Lắng nghe thay đổi Addresses từ Firebase (realtime)
     */
    private fun startAddressesRealtimeSync(userId: String) {
        val addressesRef = firebaseDatabase.getReference("addresses").child(userId)
        
        addressesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Chạy trên background thread để tránh ANR
                syncScope.launch {
                    try {
                        syncAddressesFromFirebase(userId)
                        Log.d(TAG, "Addresses data changed on Firebase, synced to SQLite")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing addresses from Firebase (realtime)", e)
                    }
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Addresses listener cancelled: ${error.message}")
            }
        }
        
        addressesRef.addValueEventListener(addressesListener!!)
    }
    
    /**
     * Lắng nghe thay đổi Wishlist từ Firebase (realtime)
     */
    private fun startWishlistRealtimeSync(userId: String) {
        val wishlistRef = firebaseDatabase.getReference("wishlist").child(userId)
        
        wishlistListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Chạy trên background thread để tránh ANR
                syncScope.launch {
                    try {
                        syncWishlistFromFirebase(userId)
                        Log.d(TAG, "Wishlist data changed on Firebase, synced to SQLite")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing wishlist from Firebase (realtime)", e)
                    }
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Wishlist listener cancelled: ${error.message}")
            }
        }
        
        wishlistRef.addValueEventListener(wishlistListener!!)
    }
    
    /**
     * Hủy tất cả các coroutines đang chạy và dừng listeners
     */
    fun cancel() {
        stopRealtimeSync()
        syncScope.cancel()
    }

    private fun getStringValue(snapshot: DataSnapshot, key: String): String {
        val value = snapshot.child(key).getValue(Any::class.java)
        return when (value) {
            is String -> value
            is Number -> value.toString()
            is Boolean -> value.toString()
            else -> ""
        }
    }

    private fun getBooleanValue(snapshot: DataSnapshot, key: String): Boolean {
        val value = snapshot.child(key).getValue(Any::class.java)
        return when (value) {
            is Boolean -> value
            is Number -> value.toInt() != 0
            is String -> value.equals("true", true) || value == "1"
            else -> false
        }
    }

    private fun getLongValue(snapshot: DataSnapshot, key: String, defaultValue: Long): Long {
        val value = snapshot.child(key).getValue(Any::class.java) ?: return defaultValue
        return when (value) {
            is Long -> value
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: defaultValue
            else -> defaultValue
        }
    }
}
