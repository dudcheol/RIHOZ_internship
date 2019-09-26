package com.example.guideline_on_camera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.guideline_on_camera.util.CameraPreview;

public class CameraActivity extends AppCompatActivity {

    private static CameraPreview surfaceView;
    private SurfaceHolder holder;
    private static Button camera_preview_button;
    private static Camera mCamera;
    private int RESULT_PERMISSIONS = 100;
    public static CameraActivity getInstance;
    private int previewState;
    private final int K_STATE_FROZEN = 0;
    private final int K_STATE_BUSY = 1;
    private final int K_STATE_PREVIEW = 2;

    private CardView shotBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CameraSetting();
        requestPermissionCamera();

        // setcontentview 다음에 find
        shotBtn = findViewById(R.id.shotBtn);


    }

    private void takePicture(CardView shotBtn) {
        shotBtn.setOnClickListener(v -> {
            switch (previewState) {
                case K_STATE_FROZEN:
                    mCamera.startPreview();
                    previewState = K_STATE_PREVIEW;
                    break;
                default:
                    //mCamera.takePicture();
                    previewState = K_STATE_BUSY;
            }
        });
    }

    public static Camera getCamera() {
        return mCamera;
    }

    private void CameraSetting() {
        // 카메라 프리뷰를  전체화면으로 보여주기 위해 셋팅한다.
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


}
