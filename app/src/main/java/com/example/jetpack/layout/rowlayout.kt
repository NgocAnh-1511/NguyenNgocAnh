package com.example.jetpack.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.navigation.NavController

@Composable
fun rowlayout(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(top=30.dp), horizontalArrangement = Arrangement.Center){
            IconButton(
                onClick = { navController.popBackStack("list_screen", inclusive = false) },
                modifier = Modifier.padding(top = 16.dp) // Thêm khoảng cách cho nút quay lại
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(32.dp), // Tăng kích thước để dễ nhìn thấy
                    tint = Color.Black // Đảm bảo màu sắc của icon rõ ràng
                )
            }


            // Tiêu đề
            Text(
                text = "Row Layout",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }


        // Các ô trong Row
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(5) { // Tạo 5 hàng
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(3) { // Tạo 3 ô trong mỗi hàng
                        Box(
                            modifier = Modifier
                                .size(100.dp, 50.dp)
                                .padding(8.dp)
                                .background(
                                    color = Color.Blue.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Box",
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

