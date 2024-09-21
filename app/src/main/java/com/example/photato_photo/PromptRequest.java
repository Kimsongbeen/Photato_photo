package com.example.photato_photo;

public class PromptRequest {
    private String text;

    public PromptRequest(String prompt) {
        this.text = prompt;
    }
    public String getPrompt() { return text;}
    public void setPrompt(String prompt) {
        this.text = prompt;
    }
}

