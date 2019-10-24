package com.example.guideline_on_camera.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.nfc.Tag;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.guideline_on_camera.CameraActivity;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static int CAMERA_FACING;
    private String TAG = "CameraPreview_Log";
    private Camera mCamera;
    private SurfaceHolder mHolder;

    // SurfaceView 생성자
    public CameraPreview(Context context, int CAMERA_FACING) {
        super(context);
        this.CAMERA_FACING = CAMERA_FACING;
        mCamera = CameraActivity.getCamera();
        if (mCamera == null) {
            mCamera = Camera.open();
        }

        mHolder = getHolder();
        mHolder.addCallback(this);

        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    //  SurfaceView 생성시 호출
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.i(TAG, "surfaceCreate 호출");

        try {
            // 카메라 객체를 사용할 수 있게 연결한다.
            if (mCamera == null) {
                mCamera = Camera.open();
            }

            // 카메라 설정
//            Camera.Parameters parameters = mCamera.getParameters();

            // 카메라 프리뷰 회전을 세로로 고정하고
            // 카메라 회전 매개 변수를 설정해서 이미지를 저장할 때 회전되서 저장되도록 함
            setCameraDisplayOrientation(CameraActivity.getInstance, CAMERA_FACING, mCamera);

//          mCamera.setParameters(parameters);

            mCamera.setPreviewDisplay(surfaceHolder);

            // 카메라 미리보기를 시작한다.
            mCamera.startPreview();

            // 자동포커스 설정
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {

                    }
                }
            });
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    // SurfaceView 의 크기가 바뀌면 호출
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int w, int h) {
        Log.i(TAG, "surfaceChanged 호출");

        // 카메라 화면을 회전 할 때의 처리
        if (surfaceHolder.getSurface() == null) {
            // 프리뷰가 존재하지 않을때
            return;
        }
        // 프리뷰를 다시 설정한다.
        try {
            mCamera.stopPreview();

            setCameraDisplayOrientation(CameraActivity.getInstance, CAMERA_FACING, mCamera);

            // 새로 변경된 설정으로 프리뷰를 시작한다
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // SurfaceView가 종료시 호출
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.i(TAG, "surfaceDestroyed 호출");

        if (mCamera != null) {
            // 카메라 미리보기를 종료한다.
            this.getHolder().removeCallback(this);
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
        int result = getCameraDisplayOrientation(activity, cameraId);

        Log.i("getCameradisplay", String.valueOf(result));

        camera.setDisplayOrientation(result);
    }

    public static int getCameraDisplayOrientation(Activity activity, int cameraId) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        Log.i("facing","camera info = " + info.facing + " / CAMERA_FACING = " + CAMERA_FACING);
        Log.i("facing", Camera.CameraInfo.CAMERA_FACING_FRONT + "< front / back >" + Camera.CameraInfo.CAMERA_FACING_BACK);
        if (CAMERA_FACING == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            Log.i("facing","front");
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
            Log.i("facing-front-result",result+"");
        } else {  // back-facing
            Log.i("facing","back");
            result = (info.orientation - degrees + 360) % 360;
            Log.i("facing-back-result",result+"");
        }
        Log.i("facing-result",result+"");
        return result;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int degree) {
        Matrix mtx = new Matrix();
        mtx.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mtx, true);
    }

    public static Bitmap invertBitmap(Bitmap bitmap) {
        Matrix mtx = new Matrix();
        mtx.setScale(1,-1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mtx, true);
    }
}
