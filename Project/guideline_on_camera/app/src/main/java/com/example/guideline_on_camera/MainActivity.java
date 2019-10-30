package com.example.guideline_on_camera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private int RESULT_CAMERA_PERMISSIONS = 100;
    private int RESULT_READ_EXTERNAL_STORAGE_PERMISSIONS = 200;
    private int RESULT_WRITE_EXTERNAL_STORAGE_PERMISSIONS = 300;
    private static final String TAG = "MAIN_LOG";
    private static int REQUEST_IMAGE_GET = 1000;
    private Button btn, sendImg, idcard_camera, backbtn;
    private ImageView selectedImage;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.go_to_camera);
        sendImg = findViewById(R.id.sendImg);
        selectedImage = findViewById(R.id.selectedImage);
        idcard_camera = findViewById(R.id.idcard_camera);
        backbtn = findViewById(R.id.backbtn);

        requestPermissionCamera();

        idcard_camera.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtra("View",1000);
            startActivity(intent);
        });

        btn.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtra("View",2000);
            startActivity(intent);
        });

        sendImg.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtra("View",3000);
            startActivity(intent);
        });

        backbtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtra("View",4000);
            startActivity(intent);
        });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }

//    private void uploadImageToServer(String imagePath) {
//        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
//
//        NetworkClient.UploadAPIs uploadAPIs = retrofit.create(NetworkClient.UploadAPIs.class);
//
//        // 파일 경로 사용해서 파일 오브젝트 생성
//        File file = new File(imagePath);
//
//        // 미디어타입 '이미지'인 리퀘스트 바디 생성
//        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"),file);
//
//        // 리퀘스트 바디와 파일명, part명을 사용해서 멀티파트바디 생성
//        MultipartBody.Part part = MultipartBody.Part.createFormData("userfile",file.getName(),fileReqBody);
//
//        // 텍스트 설명과 텍스트 미디어 타입을 사용해서 리퀘스트 바디 생성
//        RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"),"android");
//        Call call = uploadAPIs.uploadImage(part, description, null, null, null);
//
//        call.enqueue(new Callback() {
//            @Override
//            public void onResponse(Call call, Response response) {
//                if (response.isSuccessful()){
//                    Log.i("정민님의 응답",response.message());
//                    Log.i("정민님의 응답",response.toString());
//                    Toast.makeText(getApplicationContext(), "Response : 업로드 성공", Toast.LENGTH_SHORT).show();
//                }else{
//                    Toast.makeText(getApplicationContext(), "Response : 업로드 실패", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call call, Throwable t) {
//                Toast.makeText(getApplicationContext(), "Fail : 업로드 실패", Toast.LENGTH_SHORT).show();
//                Log.e("Fail",t.getMessage());
//            }
//        });
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            if (data != null) {
                Uri rcvImageUri = data.getData();
                if(rcvImageUri != null){
                    selectedImage.setImageURI(rcvImageUri);
                    Log.i(TAG,rcvImageUri.toString());
//                    uploadImageToServer(getRealPathFromURI(rcvImageUri));
                    Log.i(TAG,getRealPathFromURI(rcvImageUri));
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

    public boolean requestPermissionCamera() {
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        RESULT_CAMERA_PERMISSIONS);
            } else {
                requestPermissionStorage();
            }
        } else {  // version 6 이하일때
            return true;
        }

        return true;
    }

    public boolean requestPermissionStorage() {
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        RESULT_READ_EXTERNAL_STORAGE_PERMISSIONS);
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        RESULT_WRITE_EXTERNAL_STORAGE_PERMISSIONS);
            }
            else {
            }
        } else {  // version 6 이하일때
            return true;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if (RESULT_CAMERA_PERMISSIONS == requestCode) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestPermissionStorage();
            } else {
                // 권한 거부시
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
//                finish();
            }
            return;
        }
        if (RESULT_READ_EXTERNAL_STORAGE_PERMISSIONS == requestCode) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestPermissionStorage();
            } else {
                // 권한 거부시
                Toast.makeText(this, "외부 스토리지 읽기 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
//                finish();
            }
            return;
        }
        if (RESULT_WRITE_EXTERNAL_STORAGE_PERMISSIONS == requestCode) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                // 권한 거부시
                Toast.makeText(this, "외부 스토리지 쓰기 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
//                finish();
            }
            return;
        }
    }
}
