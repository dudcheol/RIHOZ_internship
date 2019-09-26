package com.example.guideline_on_camera;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.guideline_on_camera.network.NetworkClient;

import java.io.File;

import javax.xml.transform.Result;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private static int REQUEST_IMAGE_GET = 1000;
    private Button btn, sendImg;
    private ImageView selectedImage;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.go_to_camera);
        sendImg = findViewById(R.id.sendImg);
        selectedImage = findViewById(R.id.selectedImage);

        btn.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
        });

        sendImg.setOnClickListener(v -> {
            selectImage();
        });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }

    private void uploadImageToServer(String imagePath) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);

        NetworkClient.UploadAPIs uploadAPIs = retrofit.create(NetworkClient.UploadAPIs.class);

        // 파일 경로 사용해서 파일 오브젝트 생성
        File file = new File(imagePath);

        // 미디어타입 '이미지'인 리퀘스트 바디 생성
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"),file);

        // 리퀘스트 바디와 파일명, part명을 사용해서 멀티파트바디 생성
        MultipartBody.Part part = MultipartBody.Part.createFormData("userfile",file.getName(),fileReqBody);

        // 텍스트 설명과 텍스트 미디어 타입을 사용해서 리퀘스트 바디 생성
        RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"),"android");
        Call call = uploadAPIs.uploadImage(part, description);
        
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()){
                    Log.i("정민님의 응답",response.message());
                    Log.i("정민님의 응답",response.toString());
                    Toast.makeText(MainActivity.this, "Response : 업로드 성공", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this, "Response : 업로드 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Toast.makeText(MainActivity.this, "Fail : 업로드 실패", Toast.LENGTH_SHORT).show();
                Log.e("Fail",t.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            if (data != null) {
                Uri rcvImageUri = data.getData();
                if(rcvImageUri != null){
                    selectedImage.setImageURI(rcvImageUri);
                    uploadImageToServer(getRealPathFromURI(rcvImageUri));
                }else{
                    Toast.makeText(this, "사진첩에서 이미지를 받아오는데 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}