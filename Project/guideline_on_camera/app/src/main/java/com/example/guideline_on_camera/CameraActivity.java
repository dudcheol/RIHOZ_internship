package com.example.guideline_on_camera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.guideline_on_camera.network.NetworkClient;
import com.example.guideline_on_camera.util.CameraPreview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.view.View.getDefaultSize;
import static com.example.guideline_on_camera.util.CameraPreview.getCameraDisplayOrientation;
import static com.example.guideline_on_camera.util.CameraPreview.rotate;

public class CameraActivity extends AppCompatActivity {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final String TAG = "CAMERA_LOG";
    private static final int IMAGE_SIZE = 1024;
    public static int CAMERA_FACING;

    private CameraPreview surfaceView;
    private SurfaceHolder holder;
    private static Button camera_preview_button;
    private static Camera mCamera;
    private int RESULT_CAMERA_PERMISSIONS = 100;
    private int RESULT_READ_EXTERNAL_STORAGE_PERMISSIONS = 200;
    private int RESULT_WRITE_EXTERNAL_STORAGE_PERMISSIONS = 300;
    public static CameraActivity getInstance;
    private int previewState;
    private final int CAMERA_STATE_FROZEN = 1;
    private final int CAMERA_STATE_BUSY = 2;
    private final int CAMERA_STATE_PREVIEW = 3;

    private Button shotBtn, saveBtn, retryBtn;
    private ImageView thumbnail, guideLine;
    private RelativeLayout overlay;

    private int previewWidth;
    private int previewHeight;

    private boolean SAVE_FILE = false;

    // Todo 1 : 레트로핏으로 넘길때 url말고 바로 filestream 자체를 보낼 수 있는 방법을 생각
    // Todo 2 : 찍은 사진의 특정 영역의 rgb값을 빼와서 밝기체크 (?)
    // Todo 3 : 카메라 화질설정등 ... 하는 것 알아보기
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkCameraHardware(this);
        InitSetting();
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            Toast.makeText(getInstance, "카메라를 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
    }

    private void InitSetting() {
        CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_FRONT;
        fullScreenSetting();
        requestPermissionCamera();
    }

    private void takePicture() {
        for(int i=0;i<mCamera.getParameters().getSupportedPreviewSizes().size();i++) {
            Log.i("PreviewSizesX:",mCamera.getParameters().getSupportedPreviewSizes().get(i).width+"");
            Log.i("PreviewSizesY:",mCamera.getParameters().getSupportedPreviewSizes().get(i).height+"");
        }
        for(int i=0;i<mCamera.getParameters().getSupportedPictureSizes().size();i++) {
            Log.i("PictureSizesX:",mCamera.getParameters().getSupportedPictureSizes().get(i).width+"");
            Log.i("PictureSizesY:",mCamera.getParameters().getSupportedPictureSizes().get(i).height+"");
        }

        shotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "takePicture");
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

    // takePicture 콜백 메서드
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            Log.i("file_name",pictureFile.getPath());

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
//                fos.write(data);
//                int angleToRotate = getCameraDisplayOrientation(CameraActivity.getInstance, Camera.CameraInfo.CAMERA_FACING_FRONT);
//                // Solve image inverting problem
//                angleToRotate = angleToRotate + 90;
//                Bitmap orignalImage = BitmapFactory.decodeByteArray(data, 0, data.length);
//                Bitmap bitmapImage = rotate(orignalImage, angleToRotate);
//                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                Bitmap resultBitmap = processImage(data);
                resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();

//                Uri tempImgURI = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                String tempImgURI = pictureFile.getPath();
                Log.i("check tempimguri",tempImgURI+"");
                SAVE_FILE=false;
                changeDisplay_PICUTURED_STATE(tempImgURI);
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

//            ExifInterface exifInterface = null;
//            try {
//                exifInterface = new ExifInterface(tempImgURI.getPath());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//            Log.i("getexif-ori", String.valueOf(orientation));
        }
    };

    // 이미지나 비디오 파일을 저장하기 위한 uri 생성
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    // 이미지나 비디오 파일을 저장하기 위한 파일 생성
    private static File getOutputMediaFile(int type) {
        // Todo : 안전을 위해 이 메소드를 사용하기 전에 SDcard가 마운트 되어있는지 체크할 필요가 있음 => Environment.getExternalStorageState()

        // getExternalStoragePublicDirectory < 이 앱이 삭제되더라도 사진은 유지되는 공유 저장 공간
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "guideline_on_camera");

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
//        } else if (type == MEDIA_TYPE_VIDEO) {
//            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//                    "guideline_VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static Camera getCamera() {
        return mCamera;
    }

    private void fullScreenSetting() {
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
                        RESULT_CAMERA_PERMISSIONS);
            } else {
                requestPermissionStorage();
            }
        } else {  // version 6 이하일때
            setInit();
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
        mCamera = Camera.open(CAMERA_FACING);

        Camera.Parameters camParams = mCamera.getParameters();

        // Find a preview size that is at least the size of our IMAGE_SIZE
        Camera.Size previewSize = camParams.getSupportedPreviewSizes().get(0);
        for (Camera.Size size : camParams.getSupportedPreviewSizes()) {
            if (size.width >= IMAGE_SIZE && size.height >= IMAGE_SIZE) {
                previewSize = size;
                break;
            }
        }
        camParams.setPreviewSize(previewSize.width, previewSize.height);

        // Try to find the closest picture size to match the preview size.
        Camera.Size pictureSize = camParams.getSupportedPictureSizes().get(0);
        for (Camera.Size size : camParams.getSupportedPictureSizes()) {
            if (size.width == previewSize.width && size.height == previewSize.height) {
                pictureSize = size;
                break;
            }
        }
        camParams.setPictureSize(pictureSize.width, pictureSize.height);

        setContentView(R.layout.activity_camera);

        // SurfaceView를 상속받은 레이아웃을 정의한다.
        surfaceView = findViewById(R.id.preview);
        overlay = (RelativeLayout) findViewById(R.id.overlay);

        // SurfaceView 정의 - holder와 Callback을 정의한다.
        holder = surfaceView.getHolder();
        holder.addCallback(surfaceView);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        shotBtn = findViewById(R.id.shotBtn);
        saveBtn = findViewById(R.id.saveBtn);
        retryBtn = findViewById(R.id.retryBtn);
        thumbnail = findViewById(R.id.thumbnail);
        guideLine = findViewById(R.id.guideLine);

        // Set the height of the overlay so that it makes the preview a square
        RelativeLayout.LayoutParams overlayParams = (RelativeLayout.LayoutParams) overlay.getLayoutParams();
        overlayParams.height = previewHeight - previewWidth;
        overlay.setLayoutParams(overlayParams);

        takePicture();
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
                finish();
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
                finish();
            }
            return;
        }
        if (RESULT_WRITE_EXTERNAL_STORAGE_PERMISSIONS == requestCode) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setInit();
            } else {
                // 권한 거부시
                Toast.makeText(this, "외부 스토리지 쓰기 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
            return;
        }
    }

    // 갤러리에 추가
    private void galleryAddPic(String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.parse(path));
        this.sendBroadcast(mediaScanIntent);
    }

    private void uploadImageToServer(String imagePath) {
        shotBtn.setEnabled(false);

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

        Call call = uploadAPIs.uploadImage(part, description);

        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    Log.i("정민님의 응답", response.message());
                    Log.i("정민님의 응답", response.toString());
                    Toast.makeText(getApplicationContext(), "Response : 업로드 성공", Toast.LENGTH_SHORT).show();
                    Log.i("response_upload_fail", response.message());
                    shotBtn.setEnabled(true);
                } else {
                    Toast.makeText(getApplicationContext(), "Error Response : " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e("response_upload_fail", response.message());
                    Log.e("response_upload_fail", response.errorBody().toString());
                    shotBtn.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Fail : 업로드 실패", Toast.LENGTH_SHORT).show();
                Log.e("Fail", Objects.requireNonNull(t.getMessage()));
                shotBtn.setEnabled(true);
            }
        });
    }

//    private String changeUriForFileNameForm(Uri uri) {
//        //  일단 테스트중인 기기에선 가능하게 만듬
//        String rcvUri = uri.toString();
//        String resultUri = null;
//        int targetNum = rcvUri.indexOf("/storage/");
//        resultUri = rcvUri.substring(targetNum);
//
//        Log.i(TAG, resultUri);
//
//        return resultUri;
//    }

    private void changeDisplay_PICUTURED_STATE(String uri) {
        // 찍은 사진의 실제 경로를 구함
//        String thumbnail_uri_real_path = changeUriForFileNameForm(uri);
//        String thumbnail_uri_real_path = uri.getPath();

        displaySetting_CAPTURED_STATE();

        // 썸네일 사진 미리보기
        Glide.with(getApplicationContext())
                .load(uri)
                .into(thumbnail);

        shotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 서버로 사진 업로드
                Toast.makeText(getApplicationContext(), "사진을 서버로 전송중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show();
                uploadImageToServer(uri);
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 찍은 사진이 갤러리에도 추가되도록 한다
                galleryAddPic(uri);
                SAVE_FILE = true;
                Toast.makeText(getApplicationContext(), "사진이 갤러리에 추가되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!SAVE_FILE){
                    delete_file(uri);
                }
                displaySetting_CAPTURING_STATE();
            }
        });
    }

    private void displaySetting_CAPTURING_STATE() {
        // 카메라 프리뷰를 다시 시작함
        setInit();

        // 카메라 찍기 전 상태로 변경
        guideLine.setVisibility(View.VISIBLE);
        thumbnail.setVisibility(View.GONE);
        retryBtn.setVisibility(View.GONE);
        saveBtn.setVisibility(View.GONE);
        shotBtn.setText("cheese~~!!");

//        takePicture();
    }

    private void displaySetting_CAPTURED_STATE() {
        // 카메라 프리뷰를 멈춤
        surfaceView.surfaceDestroyed(holder);

        guideLine.setVisibility(View.GONE);
        thumbnail.setVisibility(View.VISIBLE);
        retryBtn.setVisibility(View.VISIBLE);
        saveBtn.setVisibility(View.VISIBLE);
        shotBtn.setText("서버 전송");
    }

    private void delete_file(String uri){
        File fdelete = new File(uri);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                System.out.println("file Deleted :" + uri);
            } else {
                System.out.println("file not Deleted :" + uri);
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Get the preview size
        previewWidth = surfaceView.getMeasuredWidth();
        previewHeight = surfaceView.getMeasuredHeight();

        // Set the height of the overlay so that it makes the preview a square
        RelativeLayout.LayoutParams overlayParams = (RelativeLayout.LayoutParams) overlay.getLayoutParams();
        overlayParams.height = previewHeight - previewWidth;
        overlay.setLayoutParams(overlayParams);
    }

    private Bitmap processImage(byte[] data) throws IOException {
        // Determine the width/height of the image
        int width = mCamera.getParameters().getPictureSize().width;
        int height = mCamera.getParameters().getPictureSize().height;
        int angleToRotate = getCameraDisplayOrientation(CameraActivity.getInstance, CAMERA_FACING);

        // Load the bitmap from the byte array
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap orignalImage = BitmapFactory.decodeByteArray(data, 0, data.length, options);

        int croppedWidth = (width > height) ? height : width;
        int croppedHeight = (width > height) ? height : width;

        Matrix matrix = new Matrix();
        // Solve image inverting problem
        angleToRotate = angleToRotate + 90;
        Bitmap rotatedImage = rotate(orignalImage, angleToRotate);

        // Rotate and crop the image into a square
        Bitmap cropped = Bitmap.createBitmap(rotatedImage, 0, 0, croppedWidth, croppedHeight, matrix, true);
        rotatedImage.recycle();

        // Scale down to the output size
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(cropped, IMAGE_SIZE, IMAGE_SIZE, true);
        cropped.recycle();

        return scaledBitmap;
    }

    //Todo : 앱을 나갔다가 들어올때, 앱 위에 다른 앱이 올라왔을 때 등에서
    // 카메라를 해제하고 다시 키는 작업 수행
    /*@Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            mCamera=getCameraInstance();
            mPreview = new CameraPreview(this.getActivity(), mCamera);
            preview.addView(mPreview);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null){
            surfaceView.getHolder().removeCallback(surfaceView);
            mCamera.release();
            mCamera = null;
        }
    }*/
}
