package com.example.photato_photo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class EditActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_STORAGE = 112;
    private ImageView imageView;
    private DrawView drawingView; // 사용자 정의 뷰
    private Bitmap originalBitmap;
    private Bitmap filteredBitmap; // 필터가 적용된 비트맵
    private String selectedImageUrl = null;
    private DatabaseReference postsRef;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int SELECT_FIREBASE_IMAGE_REQUEST = 2;

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
        Button buttonSelectImage = findViewById(R.id.choose_image);
        Button buttonSubmit = findViewById(R.id.save_image);
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

        // 이미지 선택 버튼 클릭 리스너
        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Firebase Storage에서 이미지 선택하는 Activity로 이동
                Intent intent = new Intent(EditActivity.this, ImageSelectionActivity.class);
                startActivityForResult(intent, SELECT_FIREBASE_IMAGE_REQUEST);
            }
        });

        // 게시글 업로드 버튼 클릭 리스너
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPost();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_FIREBASE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("selectedImageUrl")) {
                selectedImageUrl = data.getStringExtra("selectedImageUrl");
                Picasso.get().load(selectedImageUrl).into(imageView);  // 선택된 이미지 표시
            }
        }
    }

    // db 업로드 함수
    private void uploadPost() {
        if (selectedImageUrl != null) {
            // Firebase Storage에서 선택된 이미지 URL을 직접 데이터베이스에 저장
            savePostToDatabase(selectedImageUrl);
        } else {
            // 이미지가 없을 경우 null로 처리
            savePostToDatabase(null);
        }
    }

    private void savePostToDatabase(String imageUrl) {
        String postId = postsRef.push().getKey();

        Map<String, Object> post = new HashMap<>();
        post.put("imageUrl", imageUrl);

        if (postId != null) {
            postsRef.child(postId).setValue(post)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(EditActivity.this, "이미지 저장 성공.", Toast.LENGTH_SHORT).show();
                            finish();  // 이미지 저장 후 종료
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditActivity.this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
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
