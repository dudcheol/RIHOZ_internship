package com.example.guideline_on_camera;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.guideline_on_camera.VO.GetResponse;
import com.example.guideline_on_camera.network.NetworkClient;
import com.example.guideline_on_camera.util.CameraPopup;
import com.example.guideline_on_camera.util.CameraPreview;
import com.example.guideline_on_camera.util.ProgressMaterialDialog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.example.guideline_on_camera.util.CameraPreview.deviceInfo;
import static com.example.guideline_on_camera.util.CameraPreview.getCameraDisplayOrientation;

public class CameraActivity extends AppCompatActivity {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final String TAG = "CAMERA_LOG";
    private static final int IMAGE_SIZE = 720;
    private static int CAMERA_FACING;

    private CameraPreview cameraPreview;
    public static Camera mCamera;
    public static CameraActivity getInstance;
    private int previewState;
    private final int CAMERA_STATE_FROZEN = 1;
    private final int CAMERA_STATE_BUSY = 2;
    private final int CAMERA_STATE_PREVIEW = 3;

    private Button shotBtn;
    private ImageView face_line, idcard_line;
    private RelativeLayout previewArea, overlay_top, overlay_left, totalLayout;
    private TextView camera_notice;
    private RelativeLayout.LayoutParams overlayParams_top, overlayParams_bottom, overlayParams_previewArea;
    private FrameLayout cameraPreviewFrame;

    private int VIEW_TYPE;
    public final int VIEW_TYPE_IDCARD = 1000;
    public final int VIEW_TYPE_PROFILE = 2000;
    public final int VIEW_TYPE_FOREIGNCARD = 3000;
    public final int VIEW_TYPE_FOREIGNCARD_BACK = 4000;

    //view : IDCARD
    private final int IDCARD_CAMERA_VIEW = 1001;
    private final int IDCARD_CAPTURED_ERR_VIEW = 1002;

    //view : PROFILE
    private final int PROFILE_CAMERA_VIEW = 2001;
    private final int PROFILE_CAPTURED_ERR_VIEW = 2002;

    //view : FOREIGNCARD
    private final int FOREIGNCARD_CAMERA_VIEW = 3001;

    //view : FOREIGNCARD_BACK
    private final int FOREIGNCARD_BACK_CAMERA_VIEW = 4001;

    private int NUMBER_OF_OCR_TRY=0;


    MaterialDialog progressDialog = null;

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

        Log.i("생명주기확인", "onCreate");

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
        face_line = findViewById(R.id.face_line);
        camera_notice = findViewById(R.id.camera_notice);
        overlay_top = findViewById(R.id.overlay_top);
//        overlay_bottom = findViewById(R.id.overlay_bottom);
//        overlayParams_top = (RelativeLayout.LayoutParams) overlay_top.getLayoutParams();
//        overlayParams_bottom = (RelativeLayout.LayoutParams) overlay_bottom.getLayoutParams();
        previewArea = findViewById(R.id.previewArea);
        overlayParams_previewArea = (RelativeLayout.LayoutParams) previewArea.getLayoutParams();
        cameraPreviewFrame = findViewById(R.id.cameraPreviewFrame);
        overlay_left = findViewById(R.id.overlay_left);
        totalLayout = findViewById(R.id.totalLayout);
        idcard_line = findViewById(R.id.idcard_line);


        cameraPreview_open();

        switch (VIEW_TYPE){
            case VIEW_TYPE_IDCARD:
                changeViewSetting(IDCARD_CAMERA_VIEW);
                break;
            case VIEW_TYPE_PROFILE:
                changeViewSetting(PROFILE_CAMERA_VIEW);
                break;
            case VIEW_TYPE_FOREIGNCARD:
                changeViewSetting(FOREIGNCARD_CAMERA_VIEW);
                break;
            case VIEW_TYPE_FOREIGNCARD_BACK:
                changeViewSetting(FOREIGNCARD_BACK_CAMERA_VIEW);
                break;
        }
    }

    // takePicture 콜백 메서드
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // 사진 폰에 저장하려면 이 코드 주석해제
//            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
//            if (pictureFile == null) {
//                Log.d(TAG, "Error creating media file, check storage permissions");
//                return;
//            }
//            try {
//                FileOutputStream fos = new FileOutputStream(pictureFile);
//                fos.write(data);
//                fos.flush();
//                fos.close();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            showDialog(true, "사진 저장중입니다. 기다려주세요.");
            switch (VIEW_TYPE){
                case VIEW_TYPE_IDCARD:
                case VIEW_TYPE_FOREIGNCARD:
                case VIEW_TYPE_FOREIGNCARD_BACK:
                    uploadImageToServer_IDCard(data);
                    break;
                case VIEW_TYPE_PROFILE:
                    uploadImageToServer_Profile(data);
                    break;
            }
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
        // Todo : 카메라 화질관련해서 아직 미완임
        // 카메라 객체를 cameraPreview에서 먼저 정의해야 함으로 setContentView 보다 먼저 정의한다.
        getInstance = this;
        if(mCamera==null){
            switch (VIEW_TYPE) {
                case VIEW_TYPE_IDCARD: // id card
                case VIEW_TYPE_FOREIGNCARD:
                case VIEW_TYPE_FOREIGNCARD_BACK:
                    CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK;
                    mCamera = Camera.open(CAMERA_FACING);
                    break;
                case VIEW_TYPE_PROFILE: // profile
                    CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    mCamera = Camera.open(CAMERA_FACING);
                    break;
            }
            Log.i("life-cycle","camera open : "+mCamera.toString());
            Log.i("life-cycle","CAMERA_FACING : "+CAMERA_FACING);
        }
    }

    private void takePicture() {
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

    private void uploadImageToServer_IDCard(byte[] data){
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        NetworkClient.UploadIDCard uploadIDCard = retrofit.create(NetworkClient.UploadIDCard.class);
        // 미디어타입 '이미지'인 리퀘스트 바디 생성
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), data);
        // 리퀘스트 바디와 파일명, part명을 사용해서 멀티파트바디 생성 Todo : filename은 나중에 고유값으로 변경해서 보내야 할듯
        MultipartBody.Part part = MultipartBody.Part.createFormData("idCard", "picture", fileReqBody);
        HashMap<String,RequestBody> requestBodies = requestBodySetting();
        Call call = uploadIDCard.uploadImage(part, requestBodies.get("description"), requestBodies.get("angle"), requestBodies.get("top"), requestBodies.get("left"), requestBodies.get("count"));
        call.enqueue(new Callback<GetResponse>() {
            @Override
            public void onResponse(Call<GetResponse> call, Response<GetResponse> response) {
                if (response.isSuccessful()) {
                    GetResponse res = response.body();
                    if (res.isResponse()) {
                        // TYPE 을 못 받아 올 경우
                        if(res.getType().equals("error")){
                            changeViewSetting(IDCARD_CAPTURED_ERR_VIEW);
                            NUMBER_OF_OCR_TRY++;
                        } else {
                            NUMBER_OF_OCR_TRY=0;
                        }
                        // 일단 아무것도 안함
                        Toast.makeText(getInstance, res.getName()+res.getRegistrationNumFront()+res.getRegistrationNumBack()+res.getType(), Toast.LENGTH_SHORT).show();
                        Log.i("response_to_server", "response1 : " +res.getName()+res.getRegistrationNumBack()+res.getRegistrationNumFront()+res.getType());
                        Log.i("response_to_server", "response2 : " +res.getTitle()+res.getContentFirst()+res.getContentSecond()+res.getButtonText());
                    } else {
                        // Todo : response가 false 일 경우는 어떻게?
                        changeViewSetting(IDCARD_CAPTURED_ERR_VIEW);
                        Toast.makeText(getInstance, "업로드 실패!!!", Toast.LENGTH_SHORT).show();
                        Log.i("response_to_server", "response : "+res.isResponse());
                    }
                    showDialog(false, null);
                } else {
                    // Todo : 여기 에러의 경우는 어떻게 처리? 네트워크 통신 안된 것 말고도 더 있는듯
                    Log.e("response_to_server", "error msg : "+response.message());
                    Log.e("response_to_server", "error body : "+response.errorBody().toString());
                    Log.e("response_to_server", "error code : "+response.code());
                    showDialog(false, null);
                    showPopup();
                }
            }
            @Override
            public void onFailure(Call call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Fail : 업로드 실패", Toast.LENGTH_SHORT).show();
                Log.e("response_to_server",t.toString());
                showDialog(false, null);
                showPopup();
            }
        });
    }

    private void uploadImageToServer_Profile(byte[] data) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        NetworkClient.UploadProfile uploadProfile = retrofit.create(NetworkClient.UploadProfile.class);
        // 미디어타입 '이미지'인 리퀘스트 바디 생성
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), data);
        // 리퀘스트 바디와 파일명, part명을 사용해서 멀티파트바디 생성 // Todo : filename은 나중에 고유값으로 변경해서 보내야 할듯
        MultipartBody.Part part = MultipartBody.Part.createFormData("profile", "picture", fileReqBody);
        HashMap<String,RequestBody> requestBodies = requestBodySetting();
        Call call = uploadProfile.uploadImage(part, requestBodies.get("description"), requestBodies.get("angle"), requestBodies.get("top"), requestBodies.get("left"));
        call.enqueue(new Callback<GetResponse>() {
            @Override
            public void onResponse(Call<GetResponse> call, Response<GetResponse> response) {
                if (response.isSuccessful()) {
                    GetResponse res = response.body();
                    if (res.isResponse()) {
                        // 일단 아무것도 안함
                        Toast.makeText(getInstance, "업로드 성공!!!", Toast.LENGTH_SHORT).show();
                    } else {
                        changeViewSetting(PROFILE_CAPTURED_ERR_VIEW);
                    }
                    Log.i("response_to_server", res.isResponse() + "");
                    showDialog(false, null);
                } else {
                    // Todo : 여기 에러의 경우는 어떻게 처리?
                    Log.e("response_upload_fail", response.message());
                    Log.e("response_upload_fail", response.errorBody().toString());
                    showDialog(false, null);
                    showPopup();
                }
            }
            @Override
            public void onFailure(Call call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Fail : 업로드 실패", Toast.LENGTH_SHORT).show();
                Log.e("response_to_server",t.toString());
                showDialog(false, null);
                showPopup();
            }
        });
    }

    private HashMap<String,RequestBody> requestBodySetting(){
        // 텍스트 설명과 텍스트 미디어 타입을 사용해서 리퀘스트 바디 생성
        HashMap<String,RequestBody> requestBodies = new HashMap<>();
        // 텍스트 설명과 텍스트 미디어 타입을 사용해서 리퀘스트 바디 생성
        RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"), "android");
        // 앵글 값 전달
        RequestBody angle = RequestBody.create(MediaType.parse("multipart/form-data"), Integer.toString(getCameraDisplayOrientation(CameraActivity.getInstance, CAMERA_FACING)));
        // top,left 값 전달
        RequestBody left = RequestBody.create(MediaType.parse("multipart/form-data"), Integer.toString(submitCuttingInfo().x));
        RequestBody top = RequestBody.create(MediaType.parse("multipart/form-data"), Integer.toString(submitCuttingInfo().y));
        // ocr 시도 횟수 전달
        RequestBody count = RequestBody.create(MediaType.parse("multipart/form-data"), Integer.toString(NUMBER_OF_OCR_TRY));

        requestBodies.put("description",description);
        requestBodies.put("angle",angle);
        requestBodies.put("left",left);
        requestBodies.put("top",top);
        requestBodies.put("count",count);

        Log.i("request_body_setting", "get 한거 : "+requestBodies.get("description"));
        Log.i("request_body_setting", "get 안한거 : " + description);

        return requestBodies;
    }

    private void showDialog(boolean state, String text) {
        if (state && text != null) {
            ProgressMaterialDialog.Builder dialog = null;
            if (progressDialog == null) {
                dialog = new ProgressMaterialDialog.Builder(getInstance);
                progressDialog = dialog.content(text).show();
            }
        } else {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog=null;
            }
        }
    }

    private void showPopup(){
        Intent intent = new Intent(getApplicationContext(), CameraPopup.class);
        intent.putExtra("serviceCode","networkErr");
        startActivity(intent);
    }

    private Point submitCuttingInfo() {
//        Point point = getScreenSize();
        int previewSapce = (totalLayout.getHeight() - cameraPreviewFrame.getHeight()) / 2;
//        int previewSapce = (point.y - cameraPreviewFrame.getHeight())/2;
        double ratio = (double) deviceInfo.getPictureSize().width / (double) deviceInfo.getPreviewSize().width;
        Point result = new Point();
        result.x = (int) (overlay_left.getWidth() * ratio);
//        result.x = getDPtoPX(getApplicationContext(),16);
//        result.y = (int)((overlay_top.getHeight() - previewSapce) * ratio);
        result.y = (int) ((overlay_top.getHeight() - previewSapce) * ratio);


        Log.i("submitCuttingInfo", "ratio : " + ratio);
        Log.i("submitCuttingInfo", "deviceInfo.getPreviewSize().width : " + deviceInfo.getPreviewSize().width);
        Log.i("submitCuttingInfo", "deviceInfo.getPreviewSize().height : " + deviceInfo.getPreviewSize().height);
        Log.i("submitCuttingInfo", "cameraPreviewFrame.getHeight() : " + cameraPreviewFrame.getHeight());
        Log.i("submitCuttingInfo", "totalLayout.getHeight() : " + totalLayout.getHeight());
//        Log.i("submitCuttingInfo","screen height : "+point.y+" / screen width : "+point.x);
        Log.i("submitCuttingInfo", "overlay_top : " + overlay_top.getHeight());
        Log.i("submitCuttingInfo", "previewSapce : " + previewSapce);
        Log.i("submitCuttingInfo", "previewsize : " + previewArea.getHeight() + " / " + previewArea.getWidth());
        Log.i("submitCuttingInfo", "left : " + result.x);
        Log.i("submitCuttingInfo", "top : " + result.y);

        return result;
    }

    private boolean IDCard_Recognizer() {
        // Todo : 신분증 사진이 제대로 찍혔는지 확인
        return true;
    }

    private void viewSetting_allDisappear() {
        face_line.setVisibility(View.GONE);
        idcard_line.setVisibility(View.GONE);
    }

    private void changeViewSetting(int viewName) {
        viewSetting_allDisappear();

        switch (viewName) {
            case IDCARD_CAMERA_VIEW:
                idcard_line.setVisibility(View.VISIBLE);
                camera_notice.setText("글자가 잘 보이게 찍어주세요");
                break;
            case IDCARD_CAPTURED_ERR_VIEW:
                cameraReOpen();
                idcard_line.setVisibility(View.VISIBLE);
                camera_notice.setText("인식이 안돼요! 다시 찍어주세요");
                break;

            case PROFILE_CAMERA_VIEW:
                camera_notice.setText("목과 턱을 선에 맞춰 찍어주세요");
                face_line.setVisibility(View.VISIBLE);
                break;
            case PROFILE_CAPTURED_ERR_VIEW:
                cameraReOpen();
                face_line.setVisibility(View.VISIBLE);
                camera_notice.setText("사진이 흔들렸어요! 다시 찍어주세요");
                break;

            case FOREIGNCARD_BACK_CAMERA_VIEW:
                idcard_line.setVisibility(View.VISIBLE);
                camera_notice.setText("뒷면 전체가 잘 보이게 찍어주세요");
                break;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.i("생명주기확인", "onWindowFocusChanged");

        // preview의 width size 얻기
        int overlaySide_px = getDPtoPX(getApplicationContext(), 16); // dp를 px로
        Log.i("layout_check", "overlaySide_px: " + overlaySide_px);
//        int previewWidth = cameraPreview.getMeasuredWidth();
//        previewWidth -= (overlaySide_px * 2);
//        Log.i("layout_check", "previewWidth: " + previewWidth);

        // Todo : 상단바 / 네비게이터의 높이는 고려하지 않았음
        // screen의 height size 얻기
        Point point = getScreenSize();

        int screenWidth = point.x;
        int screenHeight = point.y;
        screenWidth -= (overlaySide_px * 2);
        Log.i("layout_check", "screenHeight: " + screenHeight);
        Log.i("layout_check", "screenWidth: " + screenWidth);

        switch (VIEW_TYPE) {
            case VIEW_TYPE_IDCARD:
            case VIEW_TYPE_FOREIGNCARD:
            case VIEW_TYPE_FOREIGNCARD_BACK:
//                // id card 탑, 바텀 오버레이 설정
                overlayParams_previewArea.height = (int) (screenWidth * 0.64);
                previewArea.setLayoutParams(overlayParams_previewArea);
                break;
            case VIEW_TYPE_PROFILE:
                // frofile 탑, 바텀 오버레이 설정
                overlayParams_previewArea.height = (int) (screenWidth * 1.2);
                previewArea.setLayoutParams(overlayParams_previewArea);
                break;
        }
        Log.i("layout_check", "previewHeight: " + previewArea.getHeight());
    }

    public int getDPtoPX(Context context, int dp) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager mgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mgr.getDefaultDisplay().getMetrics(metrics);
        Log.i("my_dpi", metrics.densityDpi + "");
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public Point getScreenSize() {
        Point point = new Point();
//        this.getWindowManager().getDefaultDisplay().getSize(point);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            this.getWindowManager().getDefaultDisplay().getRealSize(point);
//        } else this.getWindowManager().getDefaultDisplay().getSize(point);
        this.getWindowManager().getDefaultDisplay().getSize(point);
        return point;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera_exit:
                finish();
                break;
            case R.id.previewArea:
                mCamera.autoFocus ((success, camera) -> {
                    if(success){
                        Toast.makeText(getInstance, "focus 잡음", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }

    private void cameraPreview_open() {
        cameraPreviewFrame.removeAllViews();
        cameraPreview = new CameraPreview(this, CAMERA_FACING);
        Camera.Size previewSize = CameraPreview.getOptimalPreviewSize(mCamera.getParameters().getSupportedPreviewSizes());

        RelativeLayout.LayoutParams layoutPreviewParams = (RelativeLayout.LayoutParams) cameraPreviewFrame.getLayoutParams();
        // 카메라에서 전달해주는 width와 height는 카메라 프리뷰로 보여주는 width와 height의 반대임
        if (previewSize != null) {
            layoutPreviewParams.width = previewSize.height;
            layoutPreviewParams.height = previewSize.width;
        }
        layoutPreviewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        cameraPreviewFrame.setLayoutParams(layoutPreviewParams);
        cameraPreviewFrame.addView(cameraPreview);
    }

    private void cameraReOpen(){
        initCameraSetting();
        cameraPreview_open();
        takePicture();
    }

    //Todo : 앱을 나갔다가 들어올때, 앱 위에 다른 앱이 올라왔을 때 등에서
    // 카메라를 해제하고 다시 키는 작업 수행
    @Override
    protected void onResume() {
        super.onResume();
        Log.i("life-cycle","onResume");
        if(mCamera!=null)Log.i("life-cycle", "camera reopen : " + mCamera.toString());
        if (mCamera == null) {
            cameraReOpen();
//            cameraPreview_open();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("life-cycle","onPause");
        Log.i("life-cycle","kill camera? : "+ mCamera.toString());
        if (mCamera != null) {
            // 카메라 미리보기를 종료한다.
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }
}
