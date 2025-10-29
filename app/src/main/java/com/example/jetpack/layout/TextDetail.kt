package com.example.jetpack.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun TextDetail (navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),  // Padding cho toàn bộ Column
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
        Text(
            text = "Text Detail",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 50.dp, bottom = 30.dp),
            fontSize = 30.sp,

            textAlign = TextAlign.Center,
        )
        Text(
            text = "The quick ",
            fontSize = 18.sp,
            color = Color.Black
        )

        // Phần Text thứ hai với màu cam và kiểu chữ đậm
        Text(
            text = "Brown",
            fontSize = 18.sp,
            color = Color(0xFFFF5722), // Màu cam
            fontWeight = FontWeight.Bold
        )

        // Phần Text thứ ba với màu đen
        Text(
            text = " fox jumps over the lazy dog.",
            fontSize = 18.sp,
            color = Color.Black
        )

    }
}

