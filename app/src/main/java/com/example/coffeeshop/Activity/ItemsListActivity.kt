package com.example.coffeeshop.Activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.coffeeshop.Adapter.ItemListCategoryAdapter
import com.example.coffeeshop.R
import com.example.coffeeshop.ViewModel.MainViewModel
import com.example.coffeeshop.databinding.ActivityItemsListBinding
import com.example.coffeeshop.Manager.CartManager
import android.widget.Toast

class ItemsListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityItemsListBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var cartManager: CartManager
    private var id: String = ""
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityItemsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        cartManager = CartManager(this)

        getBundle()
        initList()


    }

    private fun initList() {
        binding.apply {
            progressBar.visibility = View.VISIBLE
            viewModel.loadItems(id).observe(this@ItemsListActivity) { items ->
                if (items != null && items.isNotEmpty()) {
                    listView.layoutManager =
                        GridLayoutManager(this@ItemsListActivity, 2)
                    listView.adapter = ItemListCategoryAdapter(items) { item ->
                        if (cartManager.addToCart(item)) {
                            Toast.makeText(this@ItemsListActivity, "${item.title} đã được thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ItemsListActivity, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                progressBar.visibility = View.GONE
            }
            backBtn.setOnClickListener { finish() }
        }
    }

    private fun getBundle() {
        id = intent.getStringExtra("id")!!
        title = intent.getStringExtra("title")!!

        binding.categoryTxt.text = title
    }
}