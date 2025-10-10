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

public class ThucHanh01 extends AppCompatActivity {
    private EditText name;
    private EditText age;
    private TextView kq;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_thuc_hanh01);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        name = findViewById(R.id.name);
        age = findViewById(R.id.editTextText2);
        kq = findViewById(R.id.ketqua);
    }

    public void kiemtra(View view) {
        String age = this.age.getText().toString();
        Double age1 = Double.parseDouble(age);
        if (age1>65) {
            kq.setText("Người già");
            kq.setVisibility(View.VISIBLE);
        }else if (age1>6) {
            kq.setText("Người lớn");
            kq.setVisibility(View.VISIBLE);
        }else if (age1>2) {
            kq.setText("Trẻ em");
            kq.setVisibility(View.VISIBLE);
        }else {
            kq.setText("Em bé");
            kq.setVisibility(View.VISIBLE);
        }

    }
}