package com.example.photato_photo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ImageSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<String> imageUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_selection);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        imageUrls = new ArrayList<>();
        imageAdapter = new ImageAdapter(imageUrls, new ImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String imageUrl) {
                // 선택된 이미지를 PostActivity로 반환
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selectedImageUrl", imageUrl);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
        recyclerView.setAdapter(imageAdapter);

        fetchImagesFromFirebaseStorage();
    }

    private void fetchImagesFromFirebaseStorage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images");

        storageRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference fileRef : listResult.getItems()) {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrls.add(uri.toString());
                    imageAdapter.notifyDataSetChanged();
                }).addOnFailureListener(e -> {
                    Toast.makeText(ImageSelectionActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(ImageSelectionActivity.this, "Failed to list files", Toast.LENGTH_SHORT).show();
        });
    }
}
