package com.example.photato_photo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PostActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent;
    private ImageView imageView;
    private Button buttonSelectImage, buttonSubmit;
    private Uri imageUri;

    private FirebaseDatabase database;
    private DatabaseReference postsRef;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private static final int PICK_IMAGE_REQUEST = 1;

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
                openFileChooser();
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

    // 이미지 선택을 위한 파일 선택기 열기
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 게시글 업로드 함수
    private void uploadPost() {
        final String title = editTextTitle.getText().toString().trim();
        final String content = editTextContent.getText().toString().trim();

        if (imageUri != null) {
            // 이미지 파일 Firebase Storage에 업로드
            final StorageReference fileRef = storageRef.child("images/" + UUID.randomUUID().toString());

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    savePostToDatabase(title, content, imageUrl);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PostActivity.this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
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
