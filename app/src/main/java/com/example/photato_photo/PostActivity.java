package com.example.photato_photo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent;
    private ImageView imageView;
    private Button buttonSelectImage, buttonSubmit;
    private String selectedImageUrl = null;

    private FirebaseDatabase database;
    private DatabaseReference postsRef;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int SELECT_FIREBASE_IMAGE_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        editTextTitle = findViewById(R.id.etTextTitle);
        editTextContent = findViewById(R.id.etTextContent);
        imageView = findViewById(R.id.imageView);
        buttonSelectImage = findViewById(R.id.btnSelectImage);
        buttonSubmit = findViewById(R.id.btnSubmit);

        // Firebase 초기화
        database = FirebaseDatabase.getInstance();
        postsRef = database.getReference("posts");
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // 이미지 선택 버튼 클릭 리스너
        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Firebase Storage에서 이미지 선택하는 Activity로 이동
                Intent intent = new Intent(PostActivity.this, ImageSelectionActivity.class);
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

    // 게시글 업로드 함수
    private void uploadPost() {
        final String title = editTextTitle.getText().toString().trim();
        final String content = editTextContent.getText().toString().trim();

        if (selectedImageUrl != null) {
            // Firebase Storage에서 선택된 이미지 URL을 직접 데이터베이스에 저장
            savePostToDatabase(title, content, selectedImageUrl);
        } else {
            // 이미지가 없을 경우 null로 처리
            savePostToDatabase(title, content, null);
        }
    }

    // 게시글 정보를 Realtime Database에 저장
    private void savePostToDatabase(String title, String content, String imageUrl) {
        String postId = postsRef.push().getKey();

        Map<String, Object> post = new HashMap<>();
        post.put("title", title);
        post.put("content", content);
        post.put("imageUrl", imageUrl);

        if (postId != null) {
            postsRef.child(postId).setValue(post)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(PostActivity.this, "게시글이 업로드되었습니다.", Toast.LENGTH_SHORT).show();
                            finish();  // 게시글 작성 후 종료
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PostActivity.this, "게시글 업로드 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
