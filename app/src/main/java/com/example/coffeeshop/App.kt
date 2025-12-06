package com.example.coffeeshop

import android.app.Application
import android.util.Log

class App : Application() {
    companion object {
        @Volatile
        private var instance: App? = null
        
        fun getInstance(): App {
            return instance ?: throw IllegalStateException("App not initialized")
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        try {
            // Không cần khởi tạo SQLite nữa, dữ liệu đã được lưu trong MySQL
            // API calls sẽ được thực hiện qua Retrofit
            Log.d("App", "App started - Using MySQL backend via API")
        } catch (e: Exception) {
            Log.e("App", "Error in onCreate", e)
        }
    }
}

