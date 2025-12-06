package com.example.coffeeshop.Manager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.coffeeshop.Database.DatabaseHelper
import com.example.coffeeshop.Domain.ItemsModel
import com.google.gson.Gson

class WishlistManager(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val gson = Gson()
    private val userManager = UserManager(context)
    private val syncManager = FirebaseSyncManager(context)

    private fun getWritableDatabase(): SQLiteDatabase {
        return dbHelper.writableDatabase
    }

    private fun getReadableDatabase(): SQLiteDatabase {
        return dbHelper.readableDatabase
    }
    
    private fun getCurrentUserId(): String? {
        return userManager.getUserId()
    }

    fun addToWishlist(item: ItemsModel): Boolean {
        val userId = getCurrentUserId() ?: return false
        val db = getWritableDatabase()
        
        // Check if already in wishlist
        val cursor = db.query(
            DatabaseHelper.TABLE_WISHLIST,
            arrayOf(DatabaseHelper.COL_WISHLIST_ID),
            "${DatabaseHelper.COL_WISHLIST_USER_ID} = ? AND ${DatabaseHelper.COL_WISHLIST_ITEM_TITLE} = ?",
            arrayOf(userId, item.title),
            null, null, null, "1"
        )
        
        if (cursor.moveToFirst()) {
            cursor.close()
            return false // Already exists
        }
        cursor.close()
        
        // Add new item
        val itemJson = gson.toJson(item)
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_WISHLIST_USER_ID, userId)
            put(DatabaseHelper.COL_WISHLIST_ITEM_TITLE, item.title)
            put(DatabaseHelper.COL_WISHLIST_ITEM_JSON, itemJson)
        }
        
        val result = db.insert(DatabaseHelper.TABLE_WISHLIST, null, values) != -1L
        if (result) {
            syncManager.syncAllDataToFirebaseAsync(userId)
        }
        return result
    }

    fun removeFromWishlist(itemTitle: String): Boolean {
        val userId = getCurrentUserId() ?: return false
        val db = getWritableDatabase()
        val result = db.delete(
            DatabaseHelper.TABLE_WISHLIST,
            "${DatabaseHelper.COL_WISHLIST_USER_ID} = ? AND ${DatabaseHelper.COL_WISHLIST_ITEM_TITLE} = ?",
            arrayOf(userId, itemTitle)
        ) > 0
        if (result) {
            syncManager.syncAllDataToFirebaseAsync(userId)
        }
        return result
    }

    fun getWishlist(): MutableList<ItemsModel> {
        val userId = getCurrentUserId() ?: return mutableListOf()
        val db = getReadableDatabase()
        val wishlist = mutableListOf<ItemsModel>()
        
        val cursor = db.query(
            DatabaseHelper.TABLE_WISHLIST,
            null,
            "${DatabaseHelper.COL_WISHLIST_USER_ID} = ?",
            arrayOf(userId),
            null, null, null, null
        )

        try {
            while (cursor.moveToNext()) {
                val itemJsonIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_ITEM_JSON)
                val itemJson = cursor.getString(itemJsonIndex) ?: continue
                
                try {
                    val item = gson.fromJson(itemJson, ItemsModel::class.java)
                    wishlist.add(item)
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
        
        return wishlist
    }

    fun isInWishlist(itemTitle: String): Boolean {
        val userId = getCurrentUserId() ?: return false
        val db = getReadableDatabase()
        val cursor = db.query(
            DatabaseHelper.TABLE_WISHLIST,
            arrayOf(DatabaseHelper.COL_WISHLIST_ID),
            "${DatabaseHelper.COL_WISHLIST_USER_ID} = ? AND ${DatabaseHelper.COL_WISHLIST_ITEM_TITLE} = ?",
            arrayOf(userId, itemTitle),
            null, null, null, "1"
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    fun clearWishlist(): Boolean {
        val userId = getCurrentUserId() ?: return false
        val db = getWritableDatabase()
        val result = db.delete(
            DatabaseHelper.TABLE_WISHLIST,
            "${DatabaseHelper.COL_WISHLIST_USER_ID} = ?",
            arrayOf(userId)
        ) >= 0
        if (result) {
            syncManager.syncAllDataToFirebaseAsync(userId)
        }
        return result
    }
}

