package com.example.aiapplication1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private Button btnChat, btnImg, btnGame ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        btnChat=findViewById(R.id.btnChat);
        btnImg=findViewById(R.id.btnImg);
        btnGame=findViewById(R.id.btnGame);
        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
        });
        btnImg.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ImgActivity.class);
            startActivity(intent);
        });
        btnGame.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });

        }






    }
