package com.example.guideline_on_camera.network;

import android.content.Context;

import com.example.guideline_on_camera.VO.GetResponse;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class NetworkClient {
    private static final String BASE_URL = "http://192.168.0.70:3000";
    private static Retrofit retrofit;
    public static Retrofit getRetrofitClient(Context context) {
        if (retrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public interface UploadProfile {
        @Multipart
        @POST("/profile")
        Call<GetResponse> uploadImage(@Part MultipartBody.Part file,
                                      @Part("description") RequestBody requestBody,
                                      @Part("angle") RequestBody angle,
                                      @Part("top") RequestBody top,
                                      @Part("left") RequestBody left);
    }

    public interface UploadIDCard {
        @Multipart
        @POST("/ocr/idRecognition")
        Call<GetResponse> uploadImage(@Part MultipartBody.Part file,
                                      @Part("description") RequestBody requestBody,
                                      @Part("angle") RequestBody angle,
                                      @Part("top") RequestBody top,
                                      @Part("left") RequestBody left,
                                      @Part("count") RequestBody count);
    }
}