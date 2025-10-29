package com.example.jetpack.layout

import ads_mobile_sdk.h6
import androidx.compose.foundation.layout.Column

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import com.example.jetpack.R
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.jetpack.ui.theme.JetpackTheme

@Composable
fun ColumnLayout (modifier: Modifier = Modifier, onButtonClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally){
        Image(
            painter = painterResource(R.drawable.jet),
            contentDescription = null,
            modifier = Modifier.padding(top = 120.dp)
        )
        Text(
            text="Jetpack Compose",
            modifier = Modifier.padding(top = 50.dp, bottom = 15.dp),
            fontWeight = FontWeight.Bold,
        )
        Text(
            text="Jetpack Compose is a modern UI toolkit for building native Android applications using a declarative programming approach.",
            textAlign= TextAlign.Center,
            modifier = Modifier.padding(start = 30.dp, end = 30.dp, bottom = 50.dp)
        )
        Button(onClick ={onButtonClick()} ) {
            Text(
                text = "I'm ready",
            )
        }
    }
}

