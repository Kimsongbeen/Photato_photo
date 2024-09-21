package com.example.photato_photo;

public class Post {
    private String title;
    private String content;
    private String imageUrl;

    public Post() {
        // Firebase에서 데이터를 가져올 때 필요한 빈 생성자
    }

    public Post(String title, String content, String imageUrl) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

