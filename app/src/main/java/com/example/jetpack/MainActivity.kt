package com.example.jetpack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jetpack.layout.ColumnLayout
import com.example.jetpack.ui.theme.JetpackTheme
import com.example.jetpack.layout.Listo
import com.example.jetpack.layout.TextDetail
import com.example.jetpack.layout.TextField
import com.example.jetpack.layout.rowlayout


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JetpackTheme {
                // Khởi tạo NavController
                val navController = rememberNavController()

                // Scaffold để chứa NavHost
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    // Điều hướng giữa các màn hình sử dụng NavHost
                    NavHost(navController = navController, startDestination = "column_layout") {
                        // Màn hình ColumnLayout
                        composable("column_layout") {
                            ColumnLayout(
                                modifier = Modifier.padding(paddingValues),
                                onButtonClick = {
                                    // Điều hướng đến màn hình Listo khi nhấn nút
                                    navController.navigate("list_screen")
                                }
                            )
                        }
                        composable("list_screen") {
                            Listo(
                                modifier = Modifier.padding(paddingValues),
                                onItemClick = { screen ->
                                    // Điều hướng đến màn hình dựa trên tên screen
                                    navController.navigate(screen)
                                }
                            )
                        }
                        composable("text_detail") {
                            TextDetail(navController)
                        }
                        composable("text_field") {
                            TextField(navController)
                        }
                        composable("text_layout") {
                            rowlayout(navController)
                        }
                    }
                }
            }
        }
    }
}
