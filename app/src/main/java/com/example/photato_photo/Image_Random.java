package com.example.photato_photo;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class Image_Random extends AppCompatActivity{

    private TextView story;
    private ImageView imageView;
    private Button generateButton;
    private Bitmap generatedImage;
    private  Button saveButton;
    private String selectedImageUrl = null;
    private DatabaseReference postsRef;

    private static final String[] NOUNS = {
            "a dragon", "snow", "moon", "light", "intricate", "elegant", "sharp focus", "beautiful dynamic",
            "highly detailed", "very sleek", "professional fine detail", "cinematic", "dramatic ambient bright colors",
            "perfect", "warm color", "epic composition", "striking", "brave", "attractive", "elite", "best", "vivid",
            "clear", "coherent", "advanced", "creative", "cute", "artistic", "trendy", "cool", "gorgeous", "awesome",
            "Apple",
            "Book",
            "Car",
            "Dog",
            "Elephant",
            "Flower",
            "Garden",
            "House",
            "Island",
            "Jungle",
            "Kite",
            "Lake",
            "Mountain",
            "Notebook",
            "Ocean",
            "Pen",
            "Queen",
            "River",
            "Sun",
            "Tree",
            "Umbrella",
            "Village",
            "Window",
            "Xylophone",
            "Yacht",
            "Zebra",
            "Ball",
            "Cat",
            "Desk",
            "Egg"
    };

    private static final String[] Adjective = {
            "Beautiful",
            "Ugly",
            "Happy",
            "Sad",
            "Angry",
            "Excited",
            "Nervous",
            "Calm",
            "Brave",
            "Cowardly",
            "Smart",
            "Stupid",
            "Tall",
            "Short",
            "Big",
            "Small",
            "Old",
            "Young",
            "New",
            "Ancient",
            "Clean",
            "Dirty",
            "Fast",
            "Slow",
            "Strong",
            "Weak",
            "Rich",
            "Poor",
            "Hot",
            "Cold"
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rgenerate);

        story = findViewById(R.id.story);
        imageView = findViewById(R.id.imageView);
        generateButton = findViewById(R.id.generateButton);
        saveButton = findViewById(R.id.saveButton);

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String prompt = generateRandomPrompt();
                story.setText(prompt);
                generateImage(prompt);
            }
        });

        saveButton.setOnClickListener(v -> {
            if (generatedImage != null) {
                saveImageToGallery(generatedImage);
                saveImageToStorage(generatedImage);
            } else {
                Toast.makeText(Image_Random.this, "No image to save", Toast.LENGTH_SHORT).show();
            }
        });

        // Generate initial image
        String initialPrompt = generateRandomPrompt();
        story.setText(initialPrompt);
        generateImage(initialPrompt);

    }

    private String generateRandomPrompt() {
        Random random = new Random();
        int index1 = random.nextInt(Adjective.length);
        int index2 = random.nextInt(NOUNS.length);
        while (index1 == index2) {
            index2 = random.nextInt(NOUNS.length);
        }
        return Adjective[index1] + " " + NOUNS[index2];
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
                            Toast.makeText(Image_Random.this, "Image generated successfully", Toast.LENGTH_LONG).show();
                            saveButton.setVisibility(View.VISIBLE);
                        });
                    } catch (IOException e) {
                        Toast.makeText(Image_Random.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(Image_Random.this, "Failed to generate image", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                String errorMsg = "Request failed: " + t.getMessage();
                Toast.makeText(Image_Random.this, errorMsg, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Request error: ", t);
            }
        });
    }
    private void saveImageToGallery(Bitmap bitmap) {
        ContentValues values = new ContentValues();
        String displayName = "GeneratedImage_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
        values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/GeneratedImages");

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    Toast.makeText(Image_Random.this, "Save Image", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(Image_Random.this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        }
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
            Toast.makeText(Image_Random.this, "Image uploaded to Firebase Storage", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(Image_Random.this, "Failed to upload image to Firebase Storage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}