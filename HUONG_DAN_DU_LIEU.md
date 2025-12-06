# Hướng Dẫn Lưu, Lấy và Đẩy Dữ Liệu trong Ứng Dụng Coffee Shop

## 📋 Tổng Quan

Ứng dụng sử dụng **3 phương thức lưu trữ chính**:
1. **SQLite Database** - Lưu trữ local trên thiết bị
2. **SharedPreferences** - Lưu trữ cấu hình và thông tin đơn giản
3. **Firebase Realtime Database** - Đồng bộ dữ liệu lên cloud
4. **REST API (Backend)** - Lưu trữ dữ liệu trên server

---

## 💾 1. LƯU DỮ LIỆU (SAVE DATA)

### 1.1. Lưu vào SQLite Database

SQLite được sử dụng để lưu trữ dữ liệu local như: Users, Cart, Orders, Addresses, Wishlist, Vouchers.

#### Cấu trúc Database:
- File: `DatabaseHelper.kt`
- Database name: `CoffeeShopDB`
- Version: 8

#### Ví dụ: Lưu sản phẩm vào giỏ hàng

```kotlin
// File: CartManager.kt
fun addToCart(item: ItemsModel): Boolean {
    val userId = getCurrentUserId() ?: return false
    val db = getWritableDatabase()
    
    // Kiểm tra sản phẩm đã có trong giỏ hàng chưa
    val cursor = db.query(
        DatabaseHelper.TABLE_CART,
        arrayOf(DatabaseHelper.COL_CART_ID, DatabaseHelper.COL_QUANTITY),
        "${DatabaseHelper.COL_CART_USER_ID} = ? AND ${DatabaseHelper.COL_ITEM_TITLE} = ?",
        arrayOf(userId, item.title),
        null, null, null, "1"
    )

    return if (cursor.moveToFirst()) {
        // Nếu đã có, tăng số lượng
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CART_ID))
        val currentQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_QUANTITY))
        cursor.close()
        
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_QUANTITY, currentQuantity + 1)
        }
        db.update(
            DatabaseHelper.TABLE_CART,
            values,
            "${DatabaseHelper.COL_CART_ID} = ?",
            arrayOf(id.toString())
        ) > 0
    } else {
        // Nếu chưa có, thêm mới
        cursor.close()
        val itemJson = gson.toJson(item)
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_CART_USER_ID, userId)
            put(DatabaseHelper.COL_ITEM_TITLE, item.title)
            put(DatabaseHelper.COL_ITEM_JSON, itemJson)
            put(DatabaseHelper.COL_QUANTITY, 1)
        }
        db.insert(DatabaseHelper.TABLE_CART, null, values) != -1L
    }
}
```

#### Các bước lưu dữ liệu vào SQLite:
1. Lấy database: `dbHelper.writableDatabase`
2. Tạo `ContentValues` với dữ liệu cần lưu
3. Sử dụng `db.insert()` để thêm mới hoặc `db.update()` để cập nhật
4. Đóng cursor và database sau khi xong

---

### 1.2. Lưu vào SharedPreferences

SharedPreferences dùng để lưu cấu hình, token, thông tin user đơn giản.

#### Ví dụ: Lưu thông tin user

```kotlin
// File: UserManager.kt
private fun saveUserToLocal(user: UserModel) {
    val prefs = context.getSharedPreferences("CoffeeShopPrefs", Context.MODE_PRIVATE)
    prefs.edit().apply {
        putString("user_id", user.userId)
        putString("phone_number", user.phoneNumber)
        putString("full_name", user.fullName)
        putString("email", user.email)
        putString("avatar_path", user.avatarPath)
        putLong("created_at", user.createdAt)
        putBoolean("is_admin", user.isAdmin)
        putBoolean("is_logged_in", true)
        apply() // Hoặc commit() nếu cần đợi kết quả
    }
}
```

#### Các phương thức lưu:
- `putString(key, value)` - Lưu chuỗi
- `putInt(key, value)` - Lưu số nguyên
- `putLong(key, value)` - Lưu số dài
- `putBoolean(key, value)` - Lưu boolean
- `putFloat(key, value)` - Lưu số thực
- `apply()` - Lưu bất đồng bộ (khuyến nghị)
- `commit()` - Lưu đồng bộ (chậm hơn)

---

### 1.3. Đẩy dữ liệu lên Firebase

Firebase được dùng để đồng bộ dữ liệu giữa các thiết bị.

#### Ví dụ: Đồng bộ giỏ hàng lên Firebase

```kotlin
// File: FirebaseSyncManager.kt
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
        
        return@withContext true
    } catch (e: Exception) {
        Log.e(TAG, "Error syncing cart to Firebase", e)
        return@withContext false
    }
}
```

#### Cách sử dụng:
```kotlin
val syncManager = FirebaseSyncManager(context)
syncManager.syncAllDataToFirebaseAsync(userId) // Đồng bộ tất cả dữ liệu
```

---

### 1.4. Đẩy dữ liệu lên Backend API

API được dùng để lưu trữ dữ liệu chính thức trên server.

#### Ví dụ: Tạo đơn hàng mới

```kotlin
// File: OrderManager.kt hoặc Activity
suspend fun createOrder(order: OrderModel): Boolean = withContext(Dispatchers.IO) {
    try {
        val apiService = ApiClient.getApiService(context)
        val token = ApiClient.getToken(context) ?: return@withContext false
        
        val request = CreateOrderRequest(
            userId = order.userId,
            totalPrice = order.totalPrice,
            deliveryAddress = order.deliveryAddress,
            phoneNumber = order.phoneNumber,
            customerName = order.customerName,
            paymentMethod = order.paymentMethod,
            items = order.items.map { cartItem ->
                OrderItemRequest(
                    productName = cartItem.item.title,
                    quantity = cartItem.quantity,
                    price = cartItem.item.price,
                    itemJson = gson.toJson(cartItem.item)
                )
            }
        )
        
        val response = apiService.createOrder("Bearer $token", request)
        
        if (response.isSuccessful && response.body() != null) {
            // Lưu vào SQLite local
            saveOrderToLocal(response.body()!!.toOrderModel(order.items))
            return@withContext true
        }
        return@withContext false
    } catch (e: Exception) {
        Log.e("OrderManager", "Error creating order", e)
        return@withContext false
    }
}
```

#### Các endpoint API chính:
- `POST /auth/login` - Đăng nhập
- `POST /auth/register` - Đăng ký
- `POST /orders` - Tạo đơn hàng
- `PATCH /orders/{orderId}` - Cập nhật đơn hàng
- `POST /vouchers` - Tạo voucher (admin)
- `PATCH /users/{userId}` - Cập nhật thông tin user

---

## 📥 2. LẤY DỮ LIỆU (RETRIEVE DATA)

### 2.1. Lấy từ SQLite Database

#### Ví dụ: Lấy danh sách giỏ hàng

```kotlin
// File: CartManager.kt
fun getCartList(): MutableList<CartModel> {
    val userId = getCurrentUserId() ?: return mutableListOf()
    val db = getReadableDatabase()
    val cartList = mutableListOf<CartModel>()
    
    val cursor = db.query(
        DatabaseHelper.TABLE_CART,
        null, // Tất cả các cột
        "${DatabaseHelper.COL_CART_USER_ID} = ?", // Điều kiện WHERE
        arrayOf(userId), // Giá trị tham số
        null, null, null, null
    )

    try {
        while (cursor.moveToNext()) {
            val itemJsonIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_JSON)
            val quantityIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_QUANTITY)
            
            val itemJson = cursor.getString(itemJsonIndex) ?: continue
            val quantity = cursor.getInt(quantityIndex)
            
            val item = gson.fromJson(itemJson, ItemsModel::class.java)
            cartList.add(CartModel(item, quantity))
        }
    } finally {
        cursor.close()
    }
    
    return cartList
}
```

#### Các phương thức query:
- `db.query()` - Query với điều kiện
- `db.rawQuery()` - Query với SQL thô
- `cursor.moveToNext()` - Di chuyển đến record tiếp theo
- `cursor.getString(index)` - Lấy giá trị String
- `cursor.getInt(index)` - Lấy giá trị Int
- `cursor.getLong(index)` - Lấy giá trị Long

---

### 2.2. Lấy từ SharedPreferences

#### Ví dụ: Lấy thông tin user

```kotlin
// File: UserManager.kt
private fun getUserFromLocal(): UserModel? {
    val prefs = context.getSharedPreferences("CoffeeShopPrefs", Context.MODE_PRIVATE)
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
```

#### Các phương thức lấy:
- `getString(key, defaultValue)` - Lấy chuỗi
- `getInt(key, defaultValue)` - Lấy số nguyên
- `getLong(key, defaultValue)` - Lấy số dài
- `getBoolean(key, defaultValue)` - Lấy boolean
- `getFloat(key, defaultValue)` - Lấy số thực

---

### 2.3. Lấy từ Firebase

#### Ví dụ: Lấy danh sách banner

```kotlin
// File: MainRepository.kt
fun loadBanner(): LiveData<MutableList<BannerModel>> {
    val listData = MutableLiveData<MutableList<BannerModel>>()
    val ref = firebaseDatabase.getReference("Banner")
    
    ref.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val list = mutableListOf<BannerModel>()
            for (childSnapshot in snapshot.children) {
                val item = childSnapshot.getValue(BannerModel::class.java)
                item?.let { list.add(it) }
            }
            listData.value = list
        }

        override fun onCancelled(error: DatabaseError) {
            // Xử lý lỗi
        }
    })
    return listData
}
```

#### Lấy dữ liệu một lần (không lắng nghe):
```kotlin
val snapshot = firebaseDatabase.getReference("users").child(userId).get().await()
if (snapshot.exists()) {
    val user = snapshot.getValue(UserModel::class.java)
}
```

---

### 2.4. Lấy từ Backend API

#### Ví dụ: Lấy danh sách sản phẩm

```kotlin
// File: MainRepository.kt
fun loadPopular(): LiveData<MutableList<ItemsModel>> {
    val listData = MutableLiveData<MutableList<ItemsModel>>()
    
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = apiService.getProducts(null)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.isNotEmpty()) {
                    val popular = body
                        .filter { it.isActive }
                        .take(8)
                        .map { it.toItemsModel() }
                    listData.postValue(popular.toMutableList())
                }
            }
        } catch (e: Exception) {
            Log.e("MainRepository", "Error loading products", e)
            listData.postValue(mutableListOf())
        }
    }
    
    return listData
}
```

#### Các endpoint lấy dữ liệu:
- `GET /public/products` - Lấy danh sách sản phẩm
- `GET /public/categories` - Lấy danh sách danh mục
- `GET /orders` - Lấy danh sách đơn hàng
- `GET /vouchers` - Lấy danh sách voucher
- `GET /auth/profile` - Lấy thông tin user hiện tại

---

## 🚀 3. ĐẨY DỮ LIỆU (PUSH DATA)

### 3.1. Đẩy lên Firebase

#### Tự động đồng bộ khi có thay đổi:

```kotlin
// File: CartManager.kt
fun addToCart(item: ItemsModel): Boolean {
    // ... lưu vào SQLite ...
    
    if (result) {
        // Tự động đồng bộ lên Firebase
        syncManager.syncAllDataToFirebaseAsync(userId)
    }
    return result
}
```

#### Đồng bộ thủ công:

```kotlin
val syncManager = FirebaseSyncManager(context)

// Đồng bộ tất cả dữ liệu
syncManager.syncAllDataToFirebaseAsync(userId)

// Đồng bộ chỉ user
syncManager.syncUserToFirebaseAsync(user)
```

#### Lắng nghe thay đổi realtime:

```kotlin
// Bắt đầu lắng nghe
syncManager.startRealtimeSync(userId)

// Dừng lắng nghe
syncManager.stopRealtimeSync()
```

---

### 3.2. Đẩy lên Backend API

#### Ví dụ: Tạo đơn hàng

```kotlin
// File: CheckoutActivity.kt hoặc OrderManager.kt
suspend fun createOrder(order: OrderModel): Boolean = withContext(Dispatchers.IO) {
    try {
        val apiService = ApiClient.getApiService(context)
        val token = ApiClient.getToken(context) ?: return@withContext false
        
        val request = CreateOrderRequest(
            userId = order.userId,
            totalPrice = order.totalPrice,
            deliveryAddress = order.deliveryAddress,
            phoneNumber = order.phoneNumber,
            customerName = order.customerName,
            paymentMethod = order.paymentMethod,
            items = order.items.map { cartItem ->
                OrderItemRequest(
                    productName = cartItem.item.title,
                    quantity = cartItem.quantity,
                    price = cartItem.item.price,
                    itemJson = gson.toJson(cartItem.item)
                )
            }
        )
        
        val response = apiService.createOrder("Bearer $token", request)
        
        return@withContext response.isSuccessful && response.body() != null
    } catch (e: Exception) {
        Log.e("OrderManager", "Error creating order", e)
        return@withContext false
    }
}
```

#### Sử dụng trong Activity/Fragment:

```kotlin
lifecycleScope.launch {
    val success = orderManager.createOrder(order)
    if (success) {
        Toast.makeText(this@CheckoutActivity, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()
        finish()
    } else {
        Toast.makeText(this@CheckoutActivity, "Đặt hàng thất bại!", Toast.LENGTH_SHORT).show()
    }
}
```

---

## 🔄 4. LUỒNG DỮ LIỆU TỔNG QUAN

### 4.1. Khi User thêm sản phẩm vào giỏ hàng:

```
User thêm sản phẩm
    ↓
Lưu vào SQLite (CartManager.addToCart)
    ↓
Tự động đồng bộ lên Firebase (FirebaseSyncManager)
    ↓
Hiển thị trên UI
```

### 4.2. Khi User đặt hàng:

```
User đặt hàng
    ↓
Gửi lên Backend API (POST /orders)
    ↓
Lưu vào SQLite local (backup)
    ↓
Đồng bộ lên Firebase (backup)
    ↓
Hiển thị thông báo thành công
```

### 4.3. Khi App khởi động:

```
App khởi động
    ↓
Lấy user từ SharedPreferences
    ↓
Nếu đã đăng nhập:
    - Đồng bộ từ Firebase về SQLite
    - Lấy dữ liệu mới từ API
    - Bắt đầu lắng nghe realtime từ Firebase
```

---

## 📝 5. CÁC MANAGER CHÍNH

### 5.1. CartManager
- **Lưu**: `addToCart()`, `updateQuantity()`
- **Lấy**: `getCartList()`, `getCartItemCount()`, `getTotalPrice()`
- **Xóa**: `removeFromCart()`, `clearCart()`

### 5.2. UserManager
- **Lưu**: `login()`, `registerUser()`, `updateUser()`
- **Lấy**: `getCurrentUser()`, `getUserId()`, `isLoggedIn()`
- **Xóa**: `logout()`

### 5.3. OrderManager
- **Lưu**: `saveOrder()`, `createOrder()` (API)
- **Lấy**: `getAllOrders()`, `getOrderById()`
- **Cập nhật**: `updateOrderStatus()`

### 5.4. FirebaseSyncManager
- **Đẩy lên**: `syncAllDataToFirebaseAsync()`, `syncUserToFirebaseAsync()`
- **Lấy về**: `syncAllDataFromFirebaseAsync()`
- **Realtime**: `startRealtimeSync()`, `stopRealtimeSync()`

### 5.5. MainRepository
- **Lấy dữ liệu**: `loadBanner()`, `loadCategory()`, `loadPopular()`, `loadNews()`
- **Nguồn**: Firebase (Banner, News) và API (Categories, Products)

---

## ⚠️ 6. LƯU Ý QUAN TRỌNG

1. **Luôn kiểm tra user đã đăng nhập** trước khi lưu vào SQLite
2. **Sử dụng Coroutines** cho các thao tác network và database
3. **Đóng cursor và database** sau khi sử dụng xong
4. **Xử lý exception** cho tất cả các thao tác network
5. **Đồng bộ dữ liệu** giữa SQLite và Firebase để backup
6. **Lưu token** vào SharedPreferences sau khi đăng nhập
7. **Sử dụng LiveData** để cập nhật UI tự động

---

## 📚 7. TÀI LIỆU THAM KHẢO

- **SQLite**: `DatabaseHelper.kt`
- **SharedPreferences**: `UserManager.kt`, `ApiClient.kt`
- **Firebase**: `FirebaseSyncManager.kt`, `MainRepository.kt`
- **API**: `ApiService.kt`, `ApiClient.kt`
- **Managers**: `CartManager.kt`, `OrderManager.kt`, `WishlistManager.kt`, `AddressManager.kt`

---

## 🎯 8. VÍ DỤ HOÀN CHỈNH

### Ví dụ: Thêm sản phẩm vào giỏ hàng và đồng bộ

```kotlin
// Trong Activity hoặc Fragment
val cartManager = CartManager(this)
val syncManager = FirebaseSyncManager(this)

// Thêm sản phẩm
val success = cartManager.addToCart(product)

if (success) {
    // Cập nhật UI
    updateCartBadge(cartManager.getCartItemCount())
    
    // Hiển thị thông báo
    Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
} else {
    Toast.makeText(this, "Lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
}
```

### Ví dụ: Lấy danh sách đơn hàng từ API

```kotlin
lifecycleScope.launch {
    try {
        val apiService = ApiClient.getApiService(this@OrderActivity)
        val token = ApiClient.getToken(this@OrderActivity) ?: return@launch
        
        val response = apiService.getOrders("Bearer $token")
        
        if (response.isSuccessful) {
            val orders = response.body()?.map { it.toOrderModel() } ?: emptyList()
            // Hiển thị lên RecyclerView
            orderAdapter.submitList(orders)
        } else {
            Toast.makeText(this@OrderActivity, "Lỗi khi tải đơn hàng", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Log.e("OrderActivity", "Error loading orders", e)
        Toast.makeText(this@OrderActivity, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
    }
}
```

---

**Chúc bạn code vui vẻ! ☕**

