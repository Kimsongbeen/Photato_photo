package com.example.photato_photo;

import static android.content.ContentValues.TAG;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Date;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class Image_Generate extends AppCompatActivity{

    private EditText promptInput;
    private Button generateButton;
    private Button saveButton;
    private ImageView imageView;
    private Bitmap generatedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_igenerate);

        promptInput = findViewById(R.id.promptInput);
        generateButton = findViewById(R.id.generateButton);
        saveButton = findViewById(R.id.saveButton);
        imageView = findViewById(R.id.imageView);

        generateButton.setOnClickListener(v -> {
            String prompt = promptInput.getText().toString();
            if (!prompt.isEmpty()) {
                generateImage(prompt);
            } else {
                Toast.makeText(Image_Generate.this, "Please enter a prompt", Toast.LENGTH_SHORT).show();
            }
        });

        saveButton.setOnClickListener(v -> {
            if (generatedImage != null) {
                saveImageToStorage(generatedImage);
            } else {
                Toast.makeText(Image_Generate.this, "No image to save", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateImage(String prompt) {
        Retrofit retrofit = UrlConfig.getRetrofitInstance();

        GenerateImageService service = retrofit.create(GenerateImageService.class);

        PromptRequest promptRequest = new PromptRequest(prompt);
        Call<ResponseBody> call = service.generateImage(promptRequest);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        byte[] imageBytes = response.body().bytes();
                        runOnUiThread(() -> {
                            // Convert byte array to Bitmap and set it to ImageView
                            generatedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            imageView.setImageBitmap(generatedImage);
                            Toast.makeText(Image_Generate.this, "Image generated successfully", Toast.LENGTH_LONG).show();
                            saveButton.setVisibility(View.VISIBLE);
                        });
                    } catch (IOException e) {
                        Toast.makeText(Image_Generate.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(Image_Generate.this, "Failed to generate image", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                String errorMsg = "Request failed: " + t.getMessage();
                Toast.makeText(Image_Generate.this, errorMsg, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Request error: ", t);
            }
        });
    }

    private void saveImageToStorage(Bitmap bitmap) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Firebase Storage에 저장될 이미지 이름 생성
        String fileName = "GeneratedImage_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
        StorageReference imagesRef = storageRef.child("images/" + fileName);

        // Bitmap을 JPEG로 압축한 후 바이트 배열로 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        // Firebase Storage로 이미지 업로드
        UploadTask uploadTask = imagesRef.putBytes(imageData);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(Image_Generate.this, "Image uploaded to Firebase Storage", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(Image_Generate.this, "Failed to upload image to Firebase Storage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}