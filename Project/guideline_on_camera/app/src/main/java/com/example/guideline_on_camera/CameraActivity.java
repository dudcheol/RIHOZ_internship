package com.example.guideline_on_camera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.guideline_on_camera.network.NetworkClient;
import com.example.guideline_on_camera.util.CameraPreview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CameraActivity extends AppCompatActivity {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final String TAG = "CAMERA_LOG";
    private int FRONT_CAMERA_ID;

    private static CameraPreview surfaceView;
    private SurfaceHolder holder;
    private static Button camera_preview_button;
    private static Camera mCamera;
    private int RESULT_PERMISSIONS = 100;
    public static CameraActivity getInstance;
    private int previewState;
    private final int CAMERA_STATE_FROZEN = 1;
    private final int CAMERA_STATE_BUSY = 2;
    private final int CAMERA_STATE_PREVIEW = 3;

    private Button shotBtn;
    private ImageView thumbnail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InitSetting();
        takePicture();

    }

    private void InitSetting() {
        FRONT_CAMERA_ID = findFrontSideCamera();
        CameraSetting();
        requestPermissionCamera();
        // setcontentview 다음에 find
        shotBtn = findViewById(R.id.shotBtn);
        thumbnail = findViewById(R.id.thumbnail);
    }

    private void takePicture() {
        shotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (previewState) {
                    case CAMERA_STATE_FROZEN:
                        mCamera.startPreview();
                        previewState = CAMERA_STATE_PREVIEW;
                        break;

                    default:
                        mCamera.takePicture(null, null, mPicture);
                        previewState = CAMERA_STATE_BUSY;

                } // switch
            }
        });
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

            Uri tempImgURI = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
            Log.i(TAG, tempImgURI.toString());

            // 찍은 사진 미리보기
            thumbnail.setImageURI(tempImgURI);
            // 찍은 사진이 갤러리에도 추가하도록 한다
            galleryAddPic(tempImgURI);

            // 찍은 사진의 회전값 조회
            String thumbnail_uri_real_path = changeUriForFileNameForm(tempImgURI);

            // 서버로 사진 업로드
            uploadImageToServer(thumbnail_uri_real_path, checkExifOrientation(thumbnail_uri_real_path));
        }
    };

    /**
     * 이미지나 비디오 파일을 저장하기 위한 uri 생성
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * 이미지나 비디오 파일을 저장하기 위한 파일 생성
     */
    private static File getOutputMediaFile(int type) {
        // Todo : 안전을 위해 이 메소드를 사용하기 전에 SDcard가 마운트 되어있는지 체크할 필요가 있음 => Environment.getExternalStorageState()

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "guideline_on_camera");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // 미디어 파일 생성
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "guideline_IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "guideline_VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static Camera getCamera() {
        return mCamera;
    }

    private void CameraSetting() {
        // 카메라 프리뷰를 전체화면으로 보여주기 위해 셋팅한다.
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public boolean requestPermissionCamera() {
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        RESULT_PERMISSIONS);
            } else {
                setInit();
            }
        } else {  // version 6 이하일때
            setInit();
            return true;
        }

        return true;
    }

    private void setInit() {
        getInstance = this;

        // 카메라 객체를 SurfaceView에서 먼저 정의해야 함으로 setContentView 보다 먼저 정의한다.
        mCamera = Camera.open();

        setContentView(R.layout.activity_camera);

        // SurfaceView를 상속받은 레이아웃을 정의한다.
        surfaceView = findViewById(R.id.preview);


        // SurfaceView 정의 - holder와 Callback을 정의한다.
        holder = surfaceView.getHolder();
        holder.addCallback(surfaceView);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if (RESULT_PERMISSIONS == requestCode) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 허가시
                setInit();
            } else {
                // 권한 거부시
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
            return;
        }
    }

    // 갤러리에 추가
    private void galleryAddPic(Uri path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(path);
        this.sendBroadcast(mediaScanIntent);
    }

    private void uploadImageToServer(String imagePath, String orientation) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);

        NetworkClient.UploadAPIs uploadAPIs = retrofit.create(NetworkClient.UploadAPIs.class);

        // 파일 경로 사용해서 파일 오브젝트 생성
        File file = new File(imagePath);

        // 미디어타입 '이미지'인 리퀘스트 바디 생성
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);

        // 리퀘스트 바디와 파일명, part명을 사용해서 멀티파트바디 생성
        MultipartBody.Part part = MultipartBody.Part.createFormData("userfile", file.getName(), fileReqBody);

        // 텍스트 설명과 텍스트 미디어 타입을 사용해서 리퀘스트 바디 생성
        RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"), "android");

        // exif orientation 바디 생성
        RequestBody exif_orientation = RequestBody.create(MediaType.parse("text"),orientation);

        // Todo : orientation도 함께 서버로 보내기
        //  checkExifOrientation() 로 반환받을 수 있음 null일경우 에러처리도 할 것

        Call call = uploadAPIs.uploadImage(part, description);

        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    Log.i("정민님의 응답", response.message());
                    Log.i("정민님의 응답", response.toString());
                    Toast.makeText(getApplicationContext(), "Response : 업로드 성공", Toast.LENGTH_SHORT).show();
                    Log.i("response_upload_fail", response.message());
                } else {
                    Toast.makeText(getApplicationContext(), "Error Response : " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e("response_upload_fail", response.message());
                    Log.e("response_upload_fail", response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Fail : 업로드 실패", Toast.LENGTH_SHORT).show();
                Log.e("Fail", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private String changeUriForFileNameForm(Uri uri) {
        //Todo : 기기별로 저장위치가 다를 수 있으므로 확인해봐야함
        //  일단 테스트중인 기기에선 가능하게 만듬
        String rcvUri = uri.toString();
        String resultUri = null;
        int targetNum = rcvUri.indexOf("/storage/");
        resultUri = rcvUri.substring(targetNum);

        Log.i(TAG, resultUri);

        return resultUri;
    }

    private int findFrontSideCamera() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }

        Log.i("front_camera", cameraId + "");
        return cameraId;
    }

    private String checkExifOrientation(String filepath) {
        String exif_orientation;
        try {
            ExifInterface exif = new ExifInterface(filepath);
            exif_orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            Log.i("EXIF", exif.getAttribute(ExifInterface.TAG_ORIENTATION));
            return exif_orientation;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error!", Toast.LENGTH_LONG).show();
        }
        return null;
    }
}
