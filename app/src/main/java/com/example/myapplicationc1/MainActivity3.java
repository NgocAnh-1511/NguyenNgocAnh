package com.example.myapplicationc1;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity3 extends AppCompatActivity {
    private EditText soLuong;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main3);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        soLuong = findViewById(R.id.editTextNumberDecimal);
    }
    public void create (View view){
        String so = soLuong.getText().toString();
        int so1 = Integer.parseInt(so);
        if (so1==1){
            findViewById(R.id.button4).setVisibility(View.VISIBLE);
            findViewById(R.id.button5).setVisibility(View.GONE);
            findViewById(R.id.button6).setVisibility(View.GONE);
            findViewById(R.id.button8).setVisibility(View.GONE);

        } else if (so1==2) {
            findViewById(R.id.button4).setVisibility(View.VISIBLE);
            findViewById(R.id.button5).setVisibility(View.VISIBLE);
            findViewById(R.id.button6).setVisibility(View.GONE);
            findViewById(R.id.button8).setVisibility(View.GONE);
        } else if (so1==3) {
            findViewById(R.id.button4).setVisibility(View.VISIBLE);
            findViewById(R.id.button5).setVisibility(View.VISIBLE);
            findViewById(R.id.button6).setVisibility(View.VISIBLE);
            findViewById(R.id.button8).setVisibility(View.GONE);
        }else {
            findViewById(R.id.button4).setVisibility(View.VISIBLE);
            findViewById(R.id.button5).setVisibility(View.VISIBLE);
            findViewById(R.id.button6).setVisibility(View.VISIBLE);
            findViewById(R.id.button8).setVisibility(View.VISIBLE);
        }
    }
}
