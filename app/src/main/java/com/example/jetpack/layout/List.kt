package com.example.jetpack.layout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Listo(modifier: Modifier = Modifier, onItemClick: (String) -> Unit) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize() // Đảm bảo LazyColumn chiếm toàn bộ không gian có sẵn
            .padding(16.dp),  // Padding cho toàn bộ Column
        verticalArrangement = Arrangement.spacedBy(8.dp) // Khoảng cách giữa các item
    ) {
        item {
            Text(
                text = "UI Components List",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 50.dp, bottom = 30.dp),
                fontSize = 30.sp,
                textAlign = TextAlign.Center,
            )
        }

        // Display section
        item {
            Text(text = "Display", fontWeight = FontWeight.Bold)
        }
        item {
            TextCard("Text", "Displays text", onItemClick, "text_detail")
        }
        item {
            TextCard("Image", "Displays an image", onItemClick, "text_detail")
        }

        // Input section
        item {
            Text(text = "Input", fontWeight = FontWeight.Bold)
        }
        item {
            TextCard("TextField", "Input field for text", onItemClick, "text_field")
        }
        item {
            TextCard("PasswordField", "Input field for passwords", onItemClick, "text_field")
        }

        // Layout section
        item {
            Text(text = "Layout", fontWeight = FontWeight.Bold)
        }
        item {
            TextCard("Column", "Arranges elements vertically", onItemClick, "text_layout")
        }
        item {
            TextCard("Row", "Arranges elements horizontally", onItemClick, "text_layout")
        }
        item {
            Text(text = "Display", fontWeight = FontWeight.Bold)
        }
        item {
            TextCard("Text", "Displays text", onItemClick, "text_detail")
        }
        item {
            TextCard("Image", "Displays an image", onItemClick, "text_detail")
        }

        // Input section
        item {
            Text(text = "Input", fontWeight = FontWeight.Bold)
        }
        item {
            TextCard("TextField", "Input field for text", onItemClick, "text_field")
        }
        item {
            TextCard("PasswordField", "Input field for passwords", onItemClick, "text_field")
        }

        // Layout section
        item {
            Text(text = "Layout", fontWeight = FontWeight.Bold)
        }
        item {
            TextCard("Column", "Arranges elements vertically", onItemClick, "text_layout")
        }
        item {
            TextCard("Row", "Arranges elements horizontally", onItemClick, "text_layout")
        }
        item {
            Text(text = "Display", fontWeight = FontWeight.Bold)
        }
        item {
            TextCard("Text", "Displays text", onItemClick, "text_detail")
        }
        item {
            TextCard("Image", "Displays an image", onItemClick, "text_detail")
        }

        // Input section
        item {
            Text(text = "Input", fontWeight = FontWeight.Bold)
        }
        item {
            TextCard("TextField", "Input field for text", onItemClick, "text_field")
        }
        item {
            TextCard("PasswordField", "Input field for passwords", onItemClick, "text_field")
        }

        // Layout section
        item {
            Text(text = "Layout", fontWeight = FontWeight.Bold)
        }
        item {
            TextCard("Column", "Arranges elements vertically", onItemClick, "text_layout")
        }
        item {
            TextCard("Row", "Arranges elements horizontally", onItemClick, "text_layout")
        }
    }
}

@Composable
fun TextCard(title: String, description: String, onItemClick: (String) -> Unit, screen: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onItemClick(screen) }),
        colors = CardDefaults.cardColors(containerColor = Color.Cyan) // Cyan background for Card
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),  // Ensure text takes up full width for centering
            )
            Text(text = description)
        }
    }
}
