package com.example.coffeeshop.Activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.coffeeshop.Adapter.BannerAdapter
import com.example.coffeeshop.Adapter.CategoryAdapter
import com.example.coffeeshop.Adapter.NewsAdapter
import com.example.coffeeshop.Adapter.PopularAdapter
import com.example.coffeeshop.Manager.CartManager
import com.example.coffeeshop.Manager.UserManager
import com.example.coffeeshop.ViewModel.MainViewModel
import com.example.coffeeshop.databinding.ActivityMainBinding
import android.widget.Toast
import android.content.Intent
import com.example.coffeeshop.R
import com.example.coffeeshop.Utils.dpToPx
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var cartManager: CartManager
    private lateinit var userManager: UserManager
    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var bestSellerAdapter: PopularAdapter
    private lateinit var forYouAdapter: PopularAdapter
    private lateinit var newsAdapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle edge-to-edge insets properly for responsive design
        ViewCompat.setOnApplyWindowInsetsListener(binding.headerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParams = v.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = systemBars.top + 16.dpToPx()
            v.layoutParams = layoutParams
            insets
        }

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        cartManager = CartManager(this)
        userManager = UserManager(this)

        setupRecyclerViews()
        loadUserInfo()
        initBanner()
        initCategory()
        initBestSeller()
        initPopular()
        initNews()
        setupNavigation()
        updateCartBadge()
    }

    override fun onResume() {
        super.onResume()
        // Cập nhật lại thông tin khi quay lại màn hình
        loadUserInfo()
        updateCartBadge()
    }

    private fun setupRecyclerViews() {
        // Setup Best Seller RecyclerView
        binding.recyclerViewBestSeller.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
        bestSellerAdapter = PopularAdapter(mutableListOf()) { item ->
            if (cartManager.addToCart(item)) {
                Toast.makeText(this, "${item.title} đã được thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                updateCartBadge()
            } else {
                Toast.makeText(this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
            }
        }
        binding.recyclerViewBestSeller.adapter = bestSellerAdapter

        // Setup For You RecyclerView
        binding.recyclerViewForYou.layoutManager = GridLayoutManager(this, 2)
        forYouAdapter = PopularAdapter(mutableListOf()) { item ->
            if (cartManager.addToCart(item)) {
                Toast.makeText(this, "${item.title} đã được thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                updateCartBadge()
            } else {
                Toast.makeText(this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
            }
        }
        binding.recyclerViewForYou.adapter = forYouAdapter

        // Setup News RecyclerView
        binding.recyclerViewNews.layoutManager = LinearLayoutManager(this)
        newsAdapter = NewsAdapter(mutableListOf())
        binding.recyclerViewNews.adapter = newsAdapter
    }

    private fun loadUserInfo() {
        val user = userManager.getCurrentUser()
        if (user != null) {
            // Hiển thị tên (nếu chưa có thì hiển thị số điện thoại)
            binding.textView2.text = if (user.fullName.isNotEmpty()) {
                user.fullName
            } else {
                user.phoneNumber
            }

            // Load và hiển thị ảnh đại diện
            if (user.avatarPath.isNotEmpty()) {
                val imageFile = File(filesDir, user.avatarPath)
                if (imageFile.exists()) {
                    Glide.with(this)
                        .load(imageFile)
                        .circleCrop()
                        .into(binding.imageView2)
                } else {
                    binding.imageView2.setImageResource(R.drawable.profile)
                }
            } else {
                binding.imageView2.setImageResource(R.drawable.profile)
            }
        } else {
            // Hiển thị "Khách" khi chưa đăng nhập
            binding.textView2.text = "Khách"
            binding.imageView2.setImageResource(R.drawable.profile)
        }
    }

    private fun updateCartBadge() {
        val cartCount = cartManager.getCartItemCount()
        if (cartCount > 0) {
            binding.cartBadge.visibility = View.VISIBLE
            binding.cartBadge.text = if (cartCount > 99) "99+" else cartCount.toString()
        } else {
            binding.cartBadge.visibility = View.GONE
        }
    }

    private fun setupNavigation() {
        binding.cartBtn.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        binding.profileBtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.orderDrinkBtn.setOnClickListener {
            val intent = Intent(this, OrderDrinkActivity::class.java)
            startActivity(intent)
        }

        binding.searchBtn.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        binding.homeBtn.setOnClickListener {
            // Already on home page, do nothing
        }

        binding.wishlistBtn.setOnClickListener {
            val intent = Intent(this, WishlistActivity::class.java)
            startActivity(intent)
        }

        binding.myOrderBtn.setOnClickListener {
            val intent = Intent(this, OrderActivity::class.java)
            startActivity(intent)
        }

        // Xem tất cả buttons
        binding.bestSellerSeeAll.setOnClickListener {
            val intent = Intent(this, OrderDrinkActivity::class.java)
            intent.putExtra("filter", "bestseller")
            startActivity(intent)
        }

        binding.forYouSeeAll.setOnClickListener {
            val intent = Intent(this, OrderDrinkActivity::class.java)
            startActivity(intent)
        }

        binding.newsSeeAll.setOnClickListener {
            // Show all news items (expand news section)
            // In a full implementation, this could navigate to a NewsListActivity
            val newsCount = newsAdapter.itemCount
            if (newsCount > 0) {
                Toast.makeText(
                    this,
                    "Đang hiển thị $newsCount tin tức",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Chưa có tin tức nào",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Bell icon (Notifications)
        binding.imageView3.setOnClickListener {
            Toast.makeText(this, "Thông báo", Toast.LENGTH_SHORT).show()
            // Could navigate to a NotificationsActivity in the future
        }

        // Settings icon
        binding.imageView4.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initPopular() {
        viewModel.loadPopular().observe(this) { items ->
            if (items != null && items.isNotEmpty()) {
                forYouAdapter.updateList(items)
            }
        }
    }

    private fun initBestSeller() {
        viewModel.loadPopular().observe(this) { items ->
            android.util.Log.d("MainActivity", "Best Seller - Received ${items?.size ?: 0} items")
            if (items != null && items.isNotEmpty()) {
                // Filter items with rating >= 4.5 for best seller
                var bestSellers = items.filter { it.rating >= 4.5 }.toMutableList()
                android.util.Log.d("MainActivity", "Best Seller - Items with rating >= 4.5: ${bestSellers.size}")

                // Nếu không có items nào có rating >= 4.5, lấy top items theo rating
                if (bestSellers.isEmpty()) {
                    // Sắp xếp theo rating giảm dần và lấy top 5
                    bestSellers = items.sortedByDescending { it.rating }
                        .take(5)
                        .toMutableList()
                    android.util.Log.d("MainActivity", "Best Seller - Using top 5 by rating: ${bestSellers.size}")
                }

                // Nếu vẫn không có, hiển thị tất cả items (có thể dữ liệu chưa có rating)
                if (bestSellers.isEmpty()) {
                    bestSellers = items.toMutableList()
                    android.util.Log.d("MainActivity", "Best Seller - Using all items: ${bestSellers.size}")
                }

                bestSellerAdapter.updateList(bestSellers)
                android.util.Log.d("MainActivity", "Best Seller - Updated adapter with ${bestSellers.size} items")
            } else {
                android.util.Log.w("MainActivity", "Best Seller - No items received or empty list")
            }
        }
    }

    private fun initCategory() {
        binding.progressBarCategory.visibility = View.VISIBLE
        viewModel.loadCategory().observe(this) { categories ->
            if (categories != null && categories.isNotEmpty()) {
                binding.categoryView.layoutManager = LinearLayoutManager(
                    this@MainActivity, LinearLayoutManager.HORIZONTAL, false
                )
                binding.categoryView.adapter = CategoryAdapter(categories)
                binding.progressBarCategory.visibility = View.GONE
            } else {
                binding.progressBarCategory.visibility = View.GONE
            }
        }
    }

    private fun initBanner() {
        binding.progressBarBanner.visibility = View.VISIBLE
        bannerAdapter = BannerAdapter(mutableListOf())
        binding.bannerViewPager.adapter = bannerAdapter

        // Setup banner indicators
        setupBannerIndicators()

        // Handle page change to update indicators
        binding.bannerViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateBannerIndicators(position)
            }
        })

        viewModel.loadBanner().observe(this) { banners ->
            if (banners != null && banners.isNotEmpty()) {
                bannerAdapter.updateList(banners)
                setupBannerIndicators()
                binding.progressBarBanner.visibility = View.GONE
            } else {
                binding.progressBarBanner.visibility = View.GONE
            }
        }
    }

    private fun setupBannerIndicators() {
        val indicatorCount = bannerAdapter.itemCount
        binding.bannerIndicator.removeAllViews()

        if (indicatorCount <= 1) return

        val indicators = arrayOfNulls<ImageView>(indicatorCount)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)

        for (i in indicators.indices) {
            indicators[i] = ImageView(this)
            indicators[i]?.layoutParams = layoutParams
            indicators[i]?.setImageResource(
                if (i == 0) R.drawable.indicator_selected
                else R.drawable.indicator_unselected
            )
            binding.bannerIndicator.addView(indicators[i])
        }
    }

    private fun updateBannerIndicators(position: Int) {
        val childCount = binding.bannerIndicator.childCount
        for (i in 0 until childCount) {
            val imageView = binding.bannerIndicator.getChildAt(i) as? ImageView
            imageView?.setImageResource(
                if (i == position) R.drawable.indicator_selected
                else R.drawable.indicator_unselected
            )
        }
    }

    private fun initNews() {
        viewModel.loadNews().observe(this) { newsList ->
            if (newsList != null && newsList.isNotEmpty()) {
                newsAdapter.updateList(newsList)
            }
        }
    }
}