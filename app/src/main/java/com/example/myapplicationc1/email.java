package com.example.myapplicationc1;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class email extends AppCompatActivity {
    private EditText concu;
    private TextView error;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_email);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        concu = findViewById(R.id.mail);
        error = findViewById(R.id.error);
    }
    public void kiemtra(View view) {
        if (concu.getText().toString().isEmpty()) {
            error.setText("Email không hợp lệ");
            error.setVisibility(View.VISIBLE);
        }else if(!concu.getText().toString().contains("@")){
            error.setText("Email không đúng định dạng");
            error.setVisibility(View.VISIBLE);
        }else {
            error.setText("Bạn đã nhập email hợp lệ");
            error.setVisibility(View.VISIBLE);

        }
    }
}