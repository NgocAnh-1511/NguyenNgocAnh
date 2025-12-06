package com.example.coffeeshop.Network

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

object ApiClient {
    // Default URL - Sửa đây khi deploy lên server công khai
    // Ví dụ: "https://your-backend.railway.app/api/"
    private const val DEFAULT_BASE_URL = "https://lttbdd-production.up.railway.app/api/" // Android emulator localhost
    // For real device on same WiFi: "http://192.168.x.x:3000/api/"
    // For production: "https://your-backend-domain.com/api/"
    
    private const val PREF_BASE_URL = "api_base_url"
    
    init {
        android.util.Log.d("ApiClient", "API Base URL: $DEFAULT_BASE_URL")
    }
    
    private var retrofit: Retrofit? = null
    private var apiService: ApiService? = null
    
    /**
     * Lấy BASE_URL từ SharedPreferences hoặc dùng DEFAULT_BASE_URL
     * Cho phép cấu hình URL động nếu cần
     */
    private fun getBaseUrl(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences("CoffeeShopPrefs", Context.MODE_PRIVATE)
        return prefs.getString(PREF_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
    }
    
    /**
     * Set custom API URL (cho phép người dùng cấu hình)
     */
    fun setBaseUrl(context: Context, url: String) {
        val prefs: SharedPreferences = context.getSharedPreferences("CoffeeShopPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_BASE_URL, url).apply()
        // Reset retrofit instance when URL changes
        retrofit = null
        apiService = null
        android.util.Log.d("ApiClient", "API Base URL updated to: $url")
    }
    
    fun getApiService(context: Context): ApiService {
        if (apiService == null) {
            val baseUrl = getBaseUrl(context)
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                    val token = getToken(context)
                    if (token != null) {
                        request.addHeader("Authorization", "Bearer $token")
                    }
                    chain.proceed(request.build())
                }
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            
            // Custom Gson với deserializer cho UserResponse.isAdmin (xử lý cả Int và Boolean)
            val gson = GsonBuilder()
                .registerTypeAdapter(com.example.coffeeshop.Network.UserResponse::class.java, UserResponseDeserializer())
                .create()
            
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            
            apiService = retrofit!!.create(ApiService::class.java)
            android.util.Log.d("ApiClient", "Retrofit initialized with URL: $baseUrl")
        }
        return apiService!!
    }
    
    fun getToken(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences("CoffeeShopPrefs", Context.MODE_PRIVATE)
        return prefs.getString("auth_token", null)
    }
    
    fun saveToken(context: Context, token: String) {
        val prefs: SharedPreferences = context.getSharedPreferences("CoffeeShopPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("auth_token", token).apply()
    }
    
    fun clearToken(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences("CoffeeShopPrefs", Context.MODE_PRIVATE)
        prefs.edit().remove("auth_token").apply()
    }
}

/**
 * Custom deserializer cho UserResponse để xử lý isAdmin có thể là Int (0/1) hoặc Boolean
 */
class UserResponseDeserializer : JsonDeserializer<com.example.coffeeshop.Network.UserResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): com.example.coffeeshop.Network.UserResponse {
        if (json == null || !json.isJsonObject) {
            throw JsonParseException("Invalid UserResponse JSON")
        }
        
        val jsonObject = json.asJsonObject
        
        // Parse isAdmin: có thể là Int, Boolean, hoặc null
        val isAdminValue: Int? = when {
            !jsonObject.has("isAdmin") || jsonObject.get("isAdmin").isJsonNull -> null
            else -> {
                val isAdminElement = jsonObject.get("isAdmin")
                when {
                    isAdminElement.isJsonPrimitive -> {
                        val primitive = isAdminElement.asJsonPrimitive
                        when {
                            primitive.isNumber -> primitive.asInt
                            primitive.isBoolean -> if (primitive.asBoolean) 1 else 0
                            else -> null
                        }
                    }
                    else -> null
                }
            }
        }
        
        fun getStringOrNull(key: String): String? {
            val element = jsonObject.get(key)
            return if (element != null && !element.isJsonNull) element.asString else null
        }
        
        return com.example.coffeeshop.Network.UserResponse(
            userId = getStringOrNull("userId") ?: "",
            phoneNumber = getStringOrNull("phoneNumber") ?: "",
            fullName = getStringOrNull("fullName"),
            email = getStringOrNull("email"),
            avatarPath = getStringOrNull("avatarPath"),
            isAdmin = isAdminValue,
            createdAt = getStringOrNull("createdAt"),
            updatedAt = getStringOrNull("updatedAt")
        )
    }
}

