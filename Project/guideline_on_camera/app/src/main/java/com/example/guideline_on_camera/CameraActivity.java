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
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
    public static final String TAG = "CAMERA_LOG";
    private static final int IMAGE_SIZE = 720;
    private static int CAMERA_FACING;

    private CameraPreview cameraPreview;
    private SurfaceHolder holder;
    private static Camera mCamera;
    public static CameraActivity getInstance;
    private int previewState;
    private final int CAMERA_STATE_FROZEN = 1;
    private final int CAMERA_STATE_BUSY = 2;
    private final int CAMERA_STATE_PREVIEW = 3;

    private Button shotBtn, retryBtn, submitBtn;
    private ImageView thumbnail, guideLine;
    private RelativeLayout overlay_top, overlay_bottom;
    private TextView camera_notice;
    private RelativeLayout.LayoutParams overlayParams_top, overlayParams_bottom;
    private LinearLayout resultBtnContainer;
    private FrameLayout cameraPreviewFrame;

    private int previewWidth;
    private int previewHeight;

    private boolean SAVE_FILE = false;
    private int VIEW_TYPE;

    //view : IDCARD
    private final int IDCARD_EXAMPLE_VIEW = 1000;
    private final int IDCARD_CAMERA_VIEW = 1001;
    private final int IDCARD_CAPTURED_ERR_VIEW = 1002;
    private final int IDCARD_CAPTURED_RESULT_VIEW = 1003;
    //view : PROFILE
    private final int PROFILE_EXAMPLE_VIEW = 2000;
    private final int PROFILE_CAMERA_VIEW = 2001;
    private final int PROFILE_CAPTURED_ERR_VIEW = 2002;
    private final int PROFILE_CAPTURED_RESULT_VIEW = 2003;

    // Todo 1 : 레트로핏으로 넘길때 url말고 바로 filestream 자체를 보낼 수 있는 방법을 생각
    // Todo 2 : 찍은 사진의 특정 영역의 rgb값을 빼와서 밝기체크 (?)
    // Todo 3 : 카메라 화질설정등 ... 하는 것 알아보기
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 카메라 프리뷰를 전체화면으로 보여주기 위해 셋팅한다.
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        // 액티비티에서 무엇을 보여줄지 결정
        // 1 : idcard
        // 2 : profile
        Intent intent = getIntent();
        VIEW_TYPE = intent.getExtras().getInt("View");
        // 카메라 객체를 SurfaceView에서 먼저 정의해야 함으로 setContentView 보다 먼저 정의한다.
        initCameraSetting();
        setContentView(R.layout.activity_camera);

        Log.i("생명주기확인","onCreate");

        checkCameraHardwareUsable(this);
        initSetting();
        takePicture();
    }

    private boolean checkCameraHardwareUsable(Context context) {
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

    private void initSetting() {
        shotBtn = findViewById(R.id.shotBtn);
        retryBtn = findViewById(R.id.retryBtn);
        submitBtn = findViewById(R.id.submitBtn);
        thumbnail = findViewById(R.id.thumbnail);
        guideLine = findViewById(R.id.guideLine);
        camera_notice = findViewById(R.id.camera_notice);
        overlay_top = findViewById(R.id.overlay_top);
        overlay_bottom = findViewById(R.id.overlay_bottom);
        overlayParams_top = (RelativeLayout.LayoutParams) overlay_top.getLayoutParams();
        overlayParams_bottom = (RelativeLayout.LayoutParams) overlay_bottom.getLayoutParams();
        resultBtnContainer = findViewById(R.id.resultBtnContainer);
        cameraPreviewFrame = findViewById(R.id.cameraPreviewFrame);

        cameraPreview_open();

//        // cameraPreview를 상속받은 레이아웃을 정의한다.
//        cameraPreview = findViewById(R.id.preview);
//        // cameraPreview 정의 - holder와 Callback을 정의한다.
//        holder = cameraPreview.getHolder();
//        holder.addCallback(cameraPreview);
//        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        changeViewSetting(IDCARD_CAMERA_VIEW);
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
//                changeDisplay_PICUTURED_STATE(tempImgURI);

                if (IDCard_Recognizer()) {
                    changeViewSetting(IDCARD_CAPTURED_RESULT_VIEW);
                } else {
                    changeViewSetting(IDCARD_CAPTURED_ERR_VIEW);
                }
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

    private void initCameraSetting() {
        switch (VIEW_TYPE){
            case 1:
                CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK;
                break;
            case 2:
                CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_FRONT;
                break;
        }
        getInstance = this;

        // 공통설정
        // 카메라 객체를 cameraPreview에서 먼저 정의해야 함으로 setContentView 보다 먼저 정의한다.
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

        shotBtn.setOnClickListener(v -> {
            // Todo : 카메라 프리뷰 스테이트를 사용하여 카메라 버튼 클릭별 동작 설정
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
        });
    }

    // 갤러리에 추가
    private void galleryAddPic(String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.parse(path));
        this.sendBroadcast(mediaScanIntent);
    }

    private void uploadImageToServer(String imagePath) {
        submitBtn.setEnabled(false);

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
                    submitBtn.setEnabled(true);
                } else {
                    Toast.makeText(getApplicationContext(), "Error Response : " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e("response_upload_fail", response.message());
                    Log.e("response_upload_fail", response.errorBody().toString());
                    submitBtn.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Fail : 업로드 실패", Toast.LENGTH_SHORT).show();
                Log.e("Fail", Objects.requireNonNull(t.getMessage()));
                submitBtn.setEnabled(true);
            }
        });
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

    private boolean IDCard_Recognizer(){
        // Todo : 신분증 사진이 제대로 찍혔는지 확인
        return true;
    }

    private void viewSetting_allDisappear(){
        thumbnail.setVisibility(View.GONE);
        shotBtn.setVisibility(View.GONE);
        guideLine.setVisibility(View.GONE);
        resultBtnContainer.setVisibility(View.GONE);
    }

    private void changeViewSetting(int viewName) {
        viewSetting_allDisappear();

        switch (viewName){
            case IDCARD_EXAMPLE_VIEW :

                break;
            case IDCARD_CAMERA_VIEW:
                shotBtn.setText("확인");
                shotBtn.setVisibility(View.VISIBLE);
                camera_notice.setText("신분증 전체가 잘 보이게 촬영해주세요");
                break;
            case IDCARD_CAPTURED_ERR_VIEW:

                break;
            case IDCARD_CAPTURED_RESULT_VIEW :
                // 카메라 프리뷰를 멈춤
                cameraPreview.surfaceDestroyed(holder);
                thumbnail.setVisibility(View.VISIBLE);
                resultBtnContainer.setVisibility(View.VISIBLE);
                camera_notice.setText("정보를 확인하세요 \n"+"다른 경우 승인이 안돼요");
                break;
            case PROFILE_EXAMPLE_VIEW :

                break;
            case PROFILE_CAMERA_VIEW:

                break;
            case PROFILE_CAPTURED_ERR_VIEW:

                break;
            case PROFILE_CAPTURED_RESULT_VIEW :

                break;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.i("생명주기확인","onWindowFocusChanged");

        // preview size 얻기
        previewWidth = cameraPreview.getMeasuredWidth();
        previewHeight = cameraPreview.getMeasuredHeight();
        Log.i("preview size",previewWidth+", "+previewHeight);

        // 탑, 바텀 오버레이 설정
        int previewSpace = previewHeight - previewWidth;
        switch (VIEW_TYPE){
            case 1:
                overlayParams_top.height = previewSpace/5*2;
                overlayParams_bottom.height = previewSpace/5*3;
                overlay_top.setLayoutParams(overlayParams_top);
                overlay_bottom.setLayoutParams(overlayParams_bottom);
                break;
            case 2:
                overlayParams_top.height = previewSpace/5*2;
                overlayParams_bottom.height = previewSpace/5*3;
                overlay_top.setLayoutParams(overlayParams_top);
                overlay_bottom.setLayoutParams(overlayParams_bottom);
                break;
        }
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.retryBtn:
                initCameraSetting();
                cameraPreview_open();
                takePicture();
                changeViewSetting(IDCARD_CAMERA_VIEW);
                break;
            case R.id.submitBtn:

                break;
            case R.id.camera_exit:
                finish();
                break;
        }
    }

    private void cameraPreview_open(){
        cameraPreviewFrame.removeAllViews();
        cameraPreview = new CameraPreview(this,CAMERA_FACING);
        cameraPreviewFrame.addView(cameraPreview);
    }

    //    private void changeDisplay_PICUTURED_STATE(String uri) {
//        // 찍은 사진의 실제 경로를 구함
////        String thumbnail_uri_real_path = changeUriForFileNameForm(uri);
////        String thumbnail_uri_real_path = uri.getPath();
//
//        displaySetting_CAPTURED_STATE();
//
//        // 썸네일 사진 미리보기
//        Glide.with(getApplicationContext())
//                .load(uri)
//                .into(thumbnail);
//
//        shotBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // 서버로 사진 업로드
//                Toast.makeText(getApplicationContext(), "사진을 서버로 전송중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show();
//                uploadImageToServer(uri);
//            }
//        });
//        saveBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // 찍은 사진이 갤러리에도 추가되도록 한다
//                galleryAddPic(uri);
//                SAVE_FILE = true;
//                Toast.makeText(getApplicationContext(), "사진이 갤러리에 추가되었습니다.", Toast.LENGTH_SHORT).show();
//            }
//        });
//        retryBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!SAVE_FILE){
//                    delete_file(uri);
//                }
//                displaySetting_CAPTURING_STATE();
//            }
//        });
//    }

//    private void displaySetting_CAPTURING_STATE() {
//        // 카메라 프리뷰를 다시 시작함
//        initCameraSetting();
//    }

//    private void displaySetting_CAPTURED_STATE() {
//        // 카메라 프리뷰를 멈춤
//        cameraPreview.surfaceDestroyed(holder);
//
//        guideLine.setVisibility(View.GONE);
//        thumbnail.setVisibility(View.VISIBLE);
//        retryBtn.setVisibility(View.VISIBLE);
//        saveBtn.setVisibility(View.VISIBLE);
//        shotBtn.setText("서버 전송");
//    }

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
        //angleToRotate = angleToRotate + 90;
        Log.i("orient",angleToRotate+"");
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
    @Override
    protected void onResume() {
        super.onResume();
        Log.i("CameraPreview_Log",mCamera.toString());
        if (mCamera == null) {
            initCameraSetting();
            cameraPreview_open();
            takePicture();
        }
    }
}
