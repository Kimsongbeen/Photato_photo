package com.example.photato_photo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Locale;

public class EditActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_STORAGE = 112;
    private ImageView imageView;
    private DrawView drawingView; // 사용자 정의 뷰
    private Bitmap originalBitmap;
    private Bitmap filteredBitmap; // 필터가 적용된 비트맵

    // 색상 목록
    private int currentColorIndex = 0;
    private int[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.BLACK}; // 사용할 색상 목록

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        imageView = findViewById(R.id.imageView);
        drawingView = findViewById(R.id.drawingView); // DrawView 초기화
        Button selectImageButton = findViewById(R.id.choose_image);
        Button saveButton = findViewById(R.id.save_image);
        Button filterNoneButton = findViewById(R.id.filterNoneButton);
        Button filterGrayscaleButton = findViewById(R.id.filterGrayscaleButton);
        Button filterSepiaButton = findViewById(R.id.filterSepiaButton);
        Button filterInvertButton = findViewById(R.id.filterInvertButton);
        Button filterBrightenButton = findViewById(R.id.filterBrightenButton);
        Button filterDarkenButton = findViewById(R.id.filterDarkenButton);
        Button changeColorButton = findViewById(R.id.changeColorButton); // 색상 변경 버튼

        // 권한 요청
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }

        // 이미지 필터 적용
        filterNoneButton.setOnClickListener(v -> applyFilter(0));
        filterGrayscaleButton.setOnClickListener(v -> applyFilter(1));
        filterSepiaButton.setOnClickListener(v -> applyFilter(2));
        filterInvertButton.setOnClickListener(v -> applyFilter(3));
        filterBrightenButton.setOnClickListener(v -> applyFilter(4));
        filterDarkenButton.setOnClickListener(v -> applyFilter(5));

        // 갤러리에서 이미지 선택
        selectImageButton.setOnClickListener(v -> openGallery());

        // 게시글 업로드 버튼 클릭 리스너
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
                saveImageToStorage(filteredBitmap);
            }
        });

        // 색상 변경
        changeColorButton.setOnClickListener(v -> changeDrawingColor());
    }

    private void openGallery() {
        ImagePicker.with(EditActivity.this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri pictureUri = data.getData();
            imageView.setImageURI(pictureUri);
            try {
                originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), pictureUri);
                filteredBitmap = originalBitmap.copy(originalBitmap.getConfig(), true); // 필터 적용을 위한 복사본
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "이미지 선택이 취소되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyFilter(int filterType) {
        if (originalBitmap == null) return;

        // 필터를 적용할 비트맵 복사본 생성
        filteredBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
        Canvas canvas = new Canvas(filteredBitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();

        switch (filterType) {
            case 0: // None
                colorMatrix.reset();
                break;
            case 1: // Grayscale
                colorMatrix.setSaturation(0);
                break;
            case 2: // Sepia
                colorMatrix.setScale(1f, 0.95f, 0.82f, 1.0f);
                break;
            case 3: // Invert
                colorMatrix.set(new float[]{
                        -1.0f, 0, 0, 0, 255,
                        0, -1.0f, 0, 0, 255,
                        0, 0, -1.0f, 0, 255,
                        0, 0, 0, 1.0f, 0
                });
                break;
            case 4: // Brighten
                colorMatrix.set(new float[]{
                        1.2f, 0, 0, 0, 0,
                        0, 1.2f, 0, 0, 0,
                        0, 0, 1.2f, 0, 0,
                        0, 0, 0, 1.0f, 0
                });
                break;
            case 5: // Darken
                colorMatrix.set(new float[]{
                        0.8f, 0, 0, 0, 0,
                        0, 0.8f, 0, 0, 0,
                        0, 0, 0.8f, 0, 0,
                        0, 0, 0, 1.0f, 0
                });
                break;
        }

        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(originalBitmap, 0, 0, paint);

        imageView.setImageBitmap(filteredBitmap); // 필터 적용된 이미지 설정
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
            Toast.makeText(EditActivity.this, "Image uploaded to Firebase Storage", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(EditActivity.this, "Failed to upload image to Firebase Storage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveImage() {
        if (imageView.getDrawable() == null && drawingView.getDrawingBitmap() == null) {
            Toast.makeText(this, "저장할 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 필터가 적용된 비트맵을 가져옵니다
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable != null ? bitmapDrawable.getBitmap() : Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);

        // `DrawView`에서 그린 그림을 비트맵으로 가져옵니다.
        Bitmap drawingBitmap = drawingView.getDrawingBitmap();

        // 비트맵에 그린 그림을 추가합니다
        if (drawingBitmap != null) {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(drawingBitmap, 0, 0, null);
        }

        OutputStream fos;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp");
                values.put(MediaStore.Images.Media.IS_PENDING, true);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "edited_image_" + System.currentTimeMillis() + ".jpg");

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                fos = getContentResolver().openOutputStream(uri);

                values.put(MediaStore.Images.Media.IS_PENDING, false);
                getContentResolver().update(uri, values, null, null);
            } else {
                File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MyApp");
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                File file = new File(directory, "edited_image_" + System.currentTimeMillis() + ".jpg");
                fos = new FileOutputStream(file);
            }

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            Toast.makeText(this, "이미지가 저장되었습니다.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "이미지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // 필터 적용된 이미지와 사용자가 그린 그림을 결합
    private Bitmap mergeDrawingAndImage() {
        if (filteredBitmap == null) return null;

        // 필터가 적용된 비트맵
        Bitmap imageBitmap = filteredBitmap;

        // DrawView에서 그린 비트맵
        Bitmap drawingBitmap;
        drawingBitmap = drawingView.getDrawingBitmap();

        // 필터 적용된 이미지와 그림을 결합
        Bitmap resultBitmap = Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight(), imageBitmap.getConfig());
        Canvas canvas = new Canvas(resultBitmap);

        // 필터 적용된 이미지 그리기
        canvas.drawBitmap(imageBitmap, 0, 0, null);

        // DrawView에서 그린 그림을 덧붙이기
        canvas.drawBitmap(drawingBitmap, 0, 0, null);

        return resultBitmap;
    }

    private void changeDrawingColor() {
        if (drawingView != null) {
            currentColorIndex = (currentColorIndex + 1) % colors.length;
            drawingView.setDrawingColor(colors[currentColorIndex]);
        } else {
            Toast.makeText(this, "DrawingView가 초기화되지 않았습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
