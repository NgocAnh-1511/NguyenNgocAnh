package com.example.jetpack.layout

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun TextField(navController: NavController) {
    // Lưu trữ giá trị nhập vào
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Padding cho toàn bộ Column
        verticalArrangement = Arrangement.Center
    ) {
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
        // TextField để người dùng nhập thông tin
        TextField(
            value = text, // Giá trị hiện tại của TextField
            onValueChange = { newText -> text = newText }, // Cập nhật khi giá trị thay đổi
            placeholder = { Text("Nhập thông tin...") }, // Placeholder (hint)
            modifier = Modifier.fillMaxWidth(), // Đảm bảo TextField chiếm hết chiều rộng
        )

        // Hiển thị giá trị nhập vào
        Spacer(modifier = Modifier.height(20.dp))
    }
}
