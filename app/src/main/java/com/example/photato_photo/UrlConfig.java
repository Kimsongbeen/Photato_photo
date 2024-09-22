package com.example.photato_photo;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class UrlConfig {
    private static final String BASE_URL = "https://0e44-35-203-180-15.ngrok-free.app/";

    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // OkHttpClient 설정 - 타임아웃 시간 60초로 설정
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)   // 연결 타임아웃
                    .readTimeout(60, TimeUnit.SECONDS)      // 읽기 타임아웃
                    .writeTimeout(60, TimeUnit.SECONDS)     // 쓰기 타임아웃
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }
}



