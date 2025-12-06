package com.example.coffeeshop.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coffeeshop.Domain.BannerModel
import com.example.coffeeshop.Domain.CategoryModel
import com.example.coffeeshop.Domain.ItemsModel
import com.example.coffeeshop.Network.ApiClient
import com.example.coffeeshop.Network.ApiService
import com.example.coffeeshop.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MainRepository {
    private val context: android.content.Context
    private val apiService: ApiService
    // Banner và News vẫn dùng Firebase tạm thời (backend chưa có API)
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    
    init {
        try {
            context = App.getInstance()
            apiService = ApiClient.getApiService(context)
            android.util.Log.d("MainRepository", "✅ MainRepository initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("MainRepository", "❌ Failed to initialize MainRepository", e)
            throw e
        }
    }

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
                // Handle error silently or log it
            }
        })
        return listData
    }

    fun loadCategory(): LiveData<MutableList<CategoryModel>> {
        val listData = MutableLiveData<MutableList<CategoryModel>>()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                android.util.Log.d("MainRepository", "Calling API: getCategories()")
                val response = apiService.getCategories()
                android.util.Log.d("MainRepository", "Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
                
                if (response.isSuccessful) {
                    val body = response.body()
                    android.util.Log.d("MainRepository", "Response body: $body")
                    
                    if (body != null && body.isNotEmpty()) {
                        val categories = body.map { it.toCategoryModel() }
                        listData.postValue(categories.toMutableList())
                        android.util.Log.d("MainRepository", "✅ Loaded ${categories.size} categories from API")
                    } else {
                        android.util.Log.w("MainRepository", "⚠️ Response body is null or empty")
                        listData.postValue(mutableListOf())
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("MainRepository", "❌ Error loading categories: ${response.code()}, error: $errorBody")
                    listData.postValue(mutableListOf())
                }
            } catch (e: Exception) {
                android.util.Log.e("MainRepository", "❌ Exception loading categories", e)
                e.printStackTrace()
                listData.postValue(mutableListOf())
            }
        }
        
        return listData
    }


    fun loadPopular(): LiveData<MutableList<ItemsModel>> {
        val listData = MutableLiveData<MutableList<ItemsModel>>()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                android.util.Log.d("MainRepository", "Calling API: getProducts(null)")
                // Lấy tất cả sản phẩm và filter popular (lấy 8 sản phẩm đầu tiên đang active)
                val response = apiService.getProducts(null)
                android.util.Log.d("MainRepository", "Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
                
                if (response.isSuccessful) {
                    val body = response.body()
                    android.util.Log.d("MainRepository", "Response body size: ${body?.size ?: 0}")
                    
                    if (body != null && body.isNotEmpty()) {
                        val popular = body
                            .filter { it.isActive }
                            .take(8)
                            .map { it.toItemsModel() }
                        listData.postValue(popular.toMutableList())
                        android.util.Log.d("MainRepository", "✅ Loaded ${popular.size} popular items from API")
                    } else {
                        android.util.Log.w("MainRepository", "⚠️ Response body is null or empty")
                        listData.postValue(mutableListOf())
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("MainRepository", "❌ Error loading popular: ${response.code()}, error: $errorBody")
                    listData.postValue(mutableListOf())
                }
            } catch (e: Exception) {
                android.util.Log.e("MainRepository", "❌ Exception loading popular", e)
                e.printStackTrace()
                listData.postValue(mutableListOf())
            }
        }
        
        return listData
    }

    fun loadItemCategory(categoryId: String): LiveData<MutableList<ItemsModel>> {
        val itemsLiveData = MutableLiveData<MutableList<ItemsModel>>()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val catId = categoryId.toIntOrNull()
                android.util.Log.d("MainRepository", "Calling API: getProducts(categoryId=$catId)")
                val response = apiService.getProducts(catId)
                android.util.Log.d("MainRepository", "Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
                
                if (response.isSuccessful) {
                    val body = response.body()
                    android.util.Log.d("MainRepository", "Response body size: ${body?.size ?: 0}")
                    
                    if (body != null && body.isNotEmpty()) {
                        val items = body.map { it.toItemsModel() }
                        itemsLiveData.postValue(items.toMutableList())
                        android.util.Log.d("MainRepository", "✅ Loaded ${items.size} items for category $categoryId from API")
                    } else {
                        android.util.Log.w("MainRepository", "⚠️ Response body is null or empty for category $categoryId")
                        itemsLiveData.postValue(mutableListOf())
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("MainRepository", "❌ Error loading items for category $categoryId: ${response.code()}, error: $errorBody")
                    itemsLiveData.postValue(mutableListOf())
                }
            } catch (e: Exception) {
                android.util.Log.e("MainRepository", "❌ Exception loading items for category $categoryId", e)
                e.printStackTrace()
                itemsLiveData.postValue(mutableListOf())
            }
        }
        
        return itemsLiveData
    }

    fun loadNews(): LiveData<MutableList<com.example.coffeeshop.Domain.NewsModel>> {
        val listData = MutableLiveData<MutableList<com.example.coffeeshop.Domain.NewsModel>>()
        val ref = firebaseDatabase.getReference("News")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<com.example.coffeeshop.Domain.NewsModel>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(com.example.coffeeshop.Domain.NewsModel::class.java)
                    item?.let { list.add(it) }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error silently or log it
            }
        })
        return listData
    }

}