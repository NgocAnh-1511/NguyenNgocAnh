package com.example.coffeeshop.Manager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.coffeeshop.Database.DatabaseHelper
import com.example.coffeeshop.Domain.AddressModel
import java.util.UUID

class AddressManager(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)
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

    fun saveAddress(address: AddressModel): Boolean {
        val userId = getCurrentUserId() ?: return false
        val db = getWritableDatabase()
        
        // If this is set as default, unset others for this user
        if (address.isDefault) {
            val values = ContentValues().apply {
                put(DatabaseHelper.COL_IS_DEFAULT, 0)
            }
            db.update(
                DatabaseHelper.TABLE_ADDRESSES,
                values,
                "${DatabaseHelper.COL_ADDRESS_USER_ID} = ? AND ${DatabaseHelper.COL_IS_DEFAULT} = ?",
                arrayOf(userId, "1")
            )
        }
        
        // If new address (no ID), generate one
        val addressId = if (address.id.isEmpty()) {
            UUID.randomUUID().toString()
        } else {
            address.id
        }
        
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_ADDRESS_ID, addressId)
            put(DatabaseHelper.COL_ADDRESS_USER_ID, userId)
            put(DatabaseHelper.COL_ADDRESS_NAME, address.name)
            put(DatabaseHelper.COL_ADDRESS_PHONE, address.phone)
            put(DatabaseHelper.COL_ADDRESS, address.address)
            put(DatabaseHelper.COL_IS_DEFAULT, if (address.isDefault) 1 else 0)
        }
        
        // Check if address exists
        val cursor = db.query(
            DatabaseHelper.TABLE_ADDRESSES,
            arrayOf(DatabaseHelper.COL_ADDRESS_ID),
            "${DatabaseHelper.COL_ADDRESS_ID} = ?",
            arrayOf(addressId),
            null, null, null, "1"
        )
        
        val exists = cursor.moveToFirst()
        cursor.close()
        
        return if (exists) {
            // Update existing
            val result = db.update(
                DatabaseHelper.TABLE_ADDRESSES,
                values,
                "${DatabaseHelper.COL_ADDRESS_ID} = ?",
                arrayOf(addressId)
            ) > 0
            if (result) {
                syncManager.syncAllDataToFirebaseAsync(userId)
            }
            result
        } else {
            // Insert new
            val result = db.insert(DatabaseHelper.TABLE_ADDRESSES, null, values) != -1L
            if (result) {
                syncManager.syncAllDataToFirebaseAsync(userId)
            }
            result
        }
    }

    fun getAllAddresses(): MutableList<AddressModel> {
        val userId = getCurrentUserId() ?: return mutableListOf()
        val db = getReadableDatabase()
        val addresses = mutableListOf<AddressModel>()
        
        val cursor = db.query(
            DatabaseHelper.TABLE_ADDRESSES,
            null,
            "${DatabaseHelper.COL_ADDRESS_USER_ID} = ?",
            arrayOf(userId),
            null, null,
            "${DatabaseHelper.COL_IS_DEFAULT} DESC, ${DatabaseHelper.COL_ADDRESS_NAME} ASC"
        )

        try {
            while (cursor.moveToNext()) {
                val idIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ADDRESS_ID)
                val nameIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ADDRESS_NAME)
                val phoneIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ADDRESS_PHONE)
                val addressIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ADDRESS)
                val isDefaultIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_IS_DEFAULT)
                
                val address = AddressModel(
                    id = cursor.getString(idIndex) ?: "",
                    name = cursor.getString(nameIndex) ?: "",
                    phone = cursor.getString(phoneIndex) ?: "",
                    address = cursor.getString(addressIndex) ?: "",
                    isDefault = cursor.getInt(isDefaultIndex) == 1
                )
                addresses.add(address)
            }
        } catch (e: Exception) {
            // Return empty list on error
        } finally {
            cursor.close()
        }
        
        return addresses
    }

    fun getDefaultAddress(): AddressModel? {
        val userId = getCurrentUserId() ?: return null
        val db = getReadableDatabase()
        val cursor = db.query(
            DatabaseHelper.TABLE_ADDRESSES,
            null,
            "${DatabaseHelper.COL_ADDRESS_USER_ID} = ? AND ${DatabaseHelper.COL_IS_DEFAULT} = ?",
            arrayOf(userId, "1"),
            null, null, null, "1"
        )

        return try {
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ADDRESS_ID)
                val nameIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ADDRESS_NAME)
                val phoneIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ADDRESS_PHONE)
                val addressIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ADDRESS)
                
                val address = AddressModel(
                    id = cursor.getString(idIndex) ?: "",
                    name = cursor.getString(nameIndex) ?: "",
                    phone = cursor.getString(phoneIndex) ?: "",
                    address = cursor.getString(addressIndex) ?: "",
                    isDefault = true
                )
                cursor.close()
                address
            } else {
                cursor.close()
                // Return first address if no default
                getAllAddresses().firstOrNull()
            }
        } catch (e: Exception) {
            cursor.close()
            getAllAddresses().firstOrNull()
        }
    }

    fun deleteAddress(addressId: String): Boolean {
        val userId = getCurrentUserId() ?: return false
        val db = getWritableDatabase()
        val result = db.delete(
            DatabaseHelper.TABLE_ADDRESSES,
            "${DatabaseHelper.COL_ADDRESS_ID} = ?",
            arrayOf(addressId)
        ) > 0
        if (result) {
            syncManager.syncAllDataToFirebaseAsync(userId)
        }
        return result
    }

    fun setDefaultAddress(addressId: String): Boolean {
        val userId = getCurrentUserId() ?: return false
        val db = getWritableDatabase()
        
        // First, unset all defaults
        val unsetValues = ContentValues().apply {
            put(DatabaseHelper.COL_IS_DEFAULT, 0)
        }
        db.update(
            DatabaseHelper.TABLE_ADDRESSES,
            unsetValues,
            "${DatabaseHelper.COL_IS_DEFAULT} = ?",
            arrayOf("1")
        )
        
        // Then set the selected address as default
        val setValues = ContentValues().apply {
            put(DatabaseHelper.COL_IS_DEFAULT, 1)
        }
        val result = db.update(
            DatabaseHelper.TABLE_ADDRESSES,
            setValues,
            "${DatabaseHelper.COL_ADDRESS_ID} = ?",
            arrayOf(addressId)
        ) > 0
        if (result) {
            syncManager.syncAllDataToFirebaseAsync(userId)
        }
        return result
    }
}

