package com.example.photato_photo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private Button generateButton;
    private Button randomButton;
    private Button editButton;
    private Button boardButton;
    private Button postButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        generateButton = findViewById(R.id.generateButton);
        editButton = findViewById(R.id.photo_edit);
        boardButton = findViewById(R.id.dashboard);
        postButton = findViewById(R.id.post);
        randomButton = findViewById(R.id.randomButton);

        generateButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Image_Generate.class);
            startActivity(intent);
        });

        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Image_Random.class);
                startActivity(intent);
            }
        });


//        editButton.setOnClickListener(v -> {
//            Intent edit = new Intent(getApplicationContext(), Photo_Edit.class);
//            startActivity(edit);
//        });
//
        boardButton.setOnClickListener(v -> {
            Intent board = new Intent(getApplicationContext(), BoardActivity.class);
            startActivity(board);
        });

        postButton.setOnClickListener(v -> {
            Intent post = new Intent(getApplicationContext(), PostActivity.class);
            startActivity(post);
        });

    }
}