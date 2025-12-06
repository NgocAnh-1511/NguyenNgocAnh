package com.example.coffeeshop.Manager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.coffeeshop.Database.DatabaseHelper
import com.example.coffeeshop.Domain.CartModel
import com.example.coffeeshop.Domain.ItemsModel
import com.google.gson.Gson

class CartManager(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val gson = Gson()
    private val userManager = UserManager(context)
    private val syncManager = FirebaseSyncManager(context)
    
    // Giỏ hàng tạm trong memory khi chưa đăng nhập
    private val tempCart: MutableList<CartModel> = mutableListOf()

    private fun getWritableDatabase(): SQLiteDatabase {
        return dbHelper.writableDatabase
    }

    private fun getReadableDatabase(): SQLiteDatabase {
        return dbHelper.readableDatabase
    }
    
    private fun getCurrentUserId(): String? {
        return userManager.getUserId()
    }
    
    private fun isLoggedIn(): Boolean {
        return userManager.isLoggedIn()
    }

    fun addToCart(item: ItemsModel): Boolean {
        // Nếu chưa đăng nhập, lưu vào memory
        if (!isLoggedIn()) {
            val existingItem = tempCart.find { it.item.title == item.title }
            if (existingItem != null) {
                existingItem.quantity++
            } else {
                tempCart.add(CartModel(item, 1))
            }
            return true
        }
        
        // Nếu đã đăng nhập, lưu vào database
        val userId = getCurrentUserId() ?: return false
        val db = getWritableDatabase()
        
        // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
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
            val result = db.update(
                DatabaseHelper.TABLE_CART,
                values,
                "${DatabaseHelper.COL_CART_ID} = ?",
                arrayOf(id.toString())
            ) > 0
            if (result) {
                syncManager.syncAllDataToFirebaseAsync(userId)
            }
            result
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
            val result = db.insert(DatabaseHelper.TABLE_CART, null, values) != -1L
            if (result) {
                syncManager.syncAllDataToFirebaseAsync(userId)
            }
            result
        }
    }

    fun removeFromCart(itemTitle: String): Boolean {
        // Nếu chưa đăng nhập, xóa khỏi memory
        if (!isLoggedIn()) {
            tempCart.removeAll { it.item.title == itemTitle }
            return true
        }
        
        // Nếu đã đăng nhập, xóa khỏi database
        val userId = getCurrentUserId() ?: return false
        val db = getWritableDatabase()
        val result = db.delete(
            DatabaseHelper.TABLE_CART,
            "${DatabaseHelper.COL_CART_USER_ID} = ? AND ${DatabaseHelper.COL_ITEM_TITLE} = ?",
            arrayOf(userId, itemTitle)
        ) > 0
        if (result) {
            syncManager.syncAllDataToFirebaseAsync(userId)
        }
        return result
    }

    fun updateQuantity(itemTitle: String, quantity: Int): Boolean {
        if (quantity <= 0) {
            return removeFromCart(itemTitle)
        }
        
        // Nếu chưa đăng nhập, cập nhật trong memory
        if (!isLoggedIn()) {
            val item = tempCart.find { it.item.title == itemTitle }
            if (item != null) {
                item.quantity = quantity
                return true
            }
            return false
        }
        
        // Nếu đã đăng nhập, cập nhật trong database
        val userId = getCurrentUserId() ?: return false
        val db = getWritableDatabase()
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_QUANTITY, quantity)
        }
        val result = db.update(
            DatabaseHelper.TABLE_CART,
            values,
            "${DatabaseHelper.COL_CART_USER_ID} = ? AND ${DatabaseHelper.COL_ITEM_TITLE} = ?",
            arrayOf(userId, itemTitle)
        ) > 0
        if (result) {
            syncManager.syncAllDataToFirebaseAsync(userId)
        }
        return result
    }

    fun getCartList(): MutableList<CartModel> {
        // Nếu chưa đăng nhập, trả về giỏ hàng tạm
        if (!isLoggedIn()) {
            return tempCart.toMutableList()
        }
        
        // Nếu đã đăng nhập, lấy từ database
        val userId = getCurrentUserId() ?: return mutableListOf()
        val db = getReadableDatabase()
        val cartList = mutableListOf<CartModel>()
        
        val cursor = db.query(
            DatabaseHelper.TABLE_CART,
            null,
            "${DatabaseHelper.COL_CART_USER_ID} = ?",
            arrayOf(userId),
            null, null, null, null
        )

        try {
            while (cursor.moveToNext()) {
                val itemJsonIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_JSON)
                val quantityIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_QUANTITY)
                
                val itemJson = cursor.getString(itemJsonIndex) ?: continue
                val quantity = cursor.getInt(quantityIndex)
                
                try {
                    val item = gson.fromJson(itemJson, ItemsModel::class.java)
                    cartList.add(CartModel(item, quantity))
                } catch (e: Exception) {
                    // Skip invalid items
                    continue
                }
            }
        } catch (e: Exception) {
            // Return empty list on error
        } finally {
            cursor.close()
        }
        
        return cartList
    }

    fun getCartItemCount(): Int {
        // Nếu chưa đăng nhập, đếm từ memory
        if (!isLoggedIn()) {
            return tempCart.sumOf { it.quantity }
        }
        
        // Nếu đã đăng nhập, đếm từ database
        val userId = getCurrentUserId() ?: return 0
        val db = getReadableDatabase()
        val cursor = db.rawQuery(
            "SELECT SUM(${DatabaseHelper.COL_QUANTITY}) FROM ${DatabaseHelper.TABLE_CART} WHERE ${DatabaseHelper.COL_CART_USER_ID} = ?",
            arrayOf(userId)
        )
        val count = if (cursor.moveToFirst() && !cursor.isNull(0)) {
            cursor.getInt(0)
        } else {
            0
        }
        cursor.close()
        return count
    }

    fun getTotalPrice(): Double {
        val cartList = getCartList()
        return cartList.sumOf { it.getTotalPrice() }
    }

    fun clearCart(): Boolean {
        // Nếu chưa đăng nhập, xóa memory
        if (!isLoggedIn()) {
            tempCart.clear()
            return true
        }
        
        // Nếu đã đăng nhập, xóa database
        val userId = getCurrentUserId() ?: return false
        val db = getWritableDatabase()
        val result = db.delete(
            DatabaseHelper.TABLE_CART,
            "${DatabaseHelper.COL_CART_USER_ID} = ?",
            arrayOf(userId)
        ) >= 0
        if (result) {
            syncManager.syncAllDataToFirebaseAsync(userId)
        }
        return result
    }
    
    /**
     * Chuyển giỏ hàng tạm vào database khi user đăng nhập
     */
    fun migrateTempCartToDatabase(userId: String): Boolean {
        if (tempCart.isEmpty()) return true
        
        val db = getWritableDatabase()
        var success = true
        
        for (cartItem in tempCart) {
            val itemJson = gson.toJson(cartItem.item)
            val values = ContentValues().apply {
                put(DatabaseHelper.COL_CART_USER_ID, userId)
                put(DatabaseHelper.COL_ITEM_TITLE, cartItem.item.title)
                put(DatabaseHelper.COL_ITEM_JSON, itemJson)
                put(DatabaseHelper.COL_QUANTITY, cartItem.quantity)
            }
            
            // Kiểm tra xem đã có trong DB chưa
            val cursor = db.query(
                DatabaseHelper.TABLE_CART,
                arrayOf(DatabaseHelper.COL_CART_ID, DatabaseHelper.COL_QUANTITY),
                "${DatabaseHelper.COL_CART_USER_ID} = ? AND ${DatabaseHelper.COL_ITEM_TITLE} = ?",
                arrayOf(userId, cartItem.item.title),
                null, null, null, "1"
            )
            
            if (cursor.moveToFirst()) {
                // Nếu đã có, cộng thêm số lượng
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CART_ID))
                val currentQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_QUANTITY))
                cursor.close()
                
                val updateValues = ContentValues().apply {
                    put(DatabaseHelper.COL_QUANTITY, currentQuantity + cartItem.quantity)
                }
                success = db.update(
                    DatabaseHelper.TABLE_CART,
                    updateValues,
                    "${DatabaseHelper.COL_CART_ID} = ?",
                    arrayOf(id.toString())
                ) > 0 && success
            } else {
                // Nếu chưa có, thêm mới
                cursor.close()
                success = (db.insert(DatabaseHelper.TABLE_CART, null, values) != -1L) && success
            }
        }
        
        // Xóa giỏ hàng tạm sau khi chuyển
        if (success) {
            tempCart.clear()
            syncManager.syncAllDataToFirebaseAsync(userId)
        }
        
        return success
    }
}

