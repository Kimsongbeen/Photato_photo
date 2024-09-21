package com.example.photato_photo;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GenerateImageService {
    @POST("/generate")
    Call<ResponseBody> generateImage(@Body PromptRequest promptRequest);
}
