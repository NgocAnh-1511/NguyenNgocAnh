# Tóm Tắt Nhanh: Lưu, Lấy và Đẩy Dữ Liệu

## 🚀 QUICK REFERENCE

### 💾 LƯU DỮ LIỆU

#### 1. SQLite (Local Database)
```kotlin
val db = DatabaseHelper(context).writableDatabase
val values = ContentValues().apply {
    put("column_name", value)
}
db.insert("table_name", null, values)
```

#### 2. SharedPreferences
```kotlin
val prefs = context.getSharedPreferences("CoffeeShopPrefs", Context.MODE_PRIVATE)
prefs.edit().apply {
    putString("key", value)
    apply()
}
```

#### 3. Firebase
```kotlin
val syncManager = FirebaseSyncManager(context)
syncManager.syncAllDataToFirebaseAsync(userId)
```

#### 4. API (Backend)
```kotlin
lifecycleScope.launch {
    val response = apiService.createOrder("Bearer $token", request)
    if (response.isSuccessful) {
        // Thành công
    }
}
```

---

### 📥 LẤY DỮ LIỆU

#### 1. SQLite
```kotlin
val db = DatabaseHelper(context).readableDatabase
val cursor = db.query("table_name", null, "where_clause", arrayOf(params), null, null, null)
while (cursor.moveToNext()) {
    val value = cursor.getString(columnIndex)
}
cursor.close()
```

#### 2. SharedPreferences
```kotlin
val prefs = context.getSharedPreferences("CoffeeShopPrefs", Context.MODE_PRIVATE)
val value = prefs.getString("key", defaultValue)
```

#### 3. Firebase
```kotlin
val ref = FirebaseDatabase.getInstance().getReference("path")
ref.addValueEventListener(object : ValueEventListener {
    override fun onDataChange(snapshot: DataSnapshot) {
        val data = snapshot.getValue(YourModel::class.java)
    }
})
```

#### 4. API
```kotlin
lifecycleScope.launch {
    val response = apiService.getProducts(null)
    if (response.isSuccessful) {
        val products = response.body()
    }
}
```

---

### 🔄 ĐẨY DỮ LIỆU

#### 1. Lên Firebase
```kotlin
syncManager.syncAllDataToFirebaseAsync(userId)
```

#### 2. Lên API
```kotlin
lifecycleScope.launch {
    val response = apiService.createOrder("Bearer $token", request)
}
```

---

## 📋 CÁC MANAGER QUAN TRỌNG

| Manager | Lưu | Lấy | Xóa |
|---------|-----|-----|-----|
| **CartManager** | `addToCart()` | `getCartList()` | `removeFromCart()` |
| **UserManager** | `login()`, `registerUser()` | `getCurrentUser()` | `logout()` |
| **OrderManager** | `saveOrder()`, `createOrder()` | `getAllOrders()` | - |
| **FirebaseSyncManager** | `syncAllDataToFirebaseAsync()` | `syncAllDataFromFirebaseAsync()` | - |

---

## 🎯 VÍ DỤ THỰC TẾ

### Thêm vào giỏ hàng:
```kotlin
val cartManager = CartManager(this)
cartManager.addToCart(product)
```

### Lấy giỏ hàng:
```kotlin
val cartList = cartManager.getCartList()
```

### Đăng nhập:
```kotlin
lifecycleScope.launch {
    val user = userManager.login(phone, password)
    if (user != null) {
        // Đăng nhập thành công
    }
}
```

### Tạo đơn hàng:
```kotlin
lifecycleScope.launch {
    val success = orderManager.createOrder(order)
}
```

---

## ⚠️ LƯU Ý

1. ✅ Luôn dùng Coroutines cho network/database
2. ✅ Đóng cursor sau khi dùng
3. ✅ Kiểm tra user đã đăng nhập
4. ✅ Xử lý exception
5. ✅ Dùng `apply()` cho SharedPreferences

---

Xem chi tiết trong file: `HUONG_DAN_DU_LIEU.md`

