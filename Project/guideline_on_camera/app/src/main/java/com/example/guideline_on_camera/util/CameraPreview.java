package com.example.guideline_on_camera.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.guideline_on_camera.CameraActivity;
import com.example.guideline_on_camera.VO.DeviceInfo;

import java.io.IOException;
import java.util.List;

import static com.example.guideline_on_camera.CameraActivity.mCamera;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    public static DeviceInfo deviceInfo;
    private static int CAMERA_FACING;
    private String TAG = "CameraPreview_Log";
    private SurfaceHolder mHolder;
    private Camera.Size previewSize;
    private Camera.Parameters camParams;

    // SurfaceView 생성자
    public CameraPreview(Context context, int CAMERA_FACING) {
        super(context);
        this.CAMERA_FACING = CAMERA_FACING;
        if (mCamera == null) {
            mCamera = Camera.open(CAMERA_FACING);
            Log.i("life-cycle","camera Change : "+mCamera.toString());
        }
        Log.i("life-cycle","camera preview CAMERA_FACING : "+CAMERA_FACING);

        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    //  SurfaceView 생성시 호출
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.i(TAG, "surfaceCreate 호출");
        deviceInfo = new DeviceInfo();

        try {
            // 카메라 객체를 사용할 수 있게 연결한다.
            if (mCamera == null) {
                mCamera = Camera.open();
            }
            camParams = mCamera.getParameters();

            // 그냥 확인용
            for(Camera.Size size : camParams.getSupportedPictureSizes()){
                Log.d("CameraPreview_Log", "    supported picture size: " + size.width + ", " + size.height);
            }

            // 사용중인 기기 화면에 맞는 프리뷰 사이즈 설정
            previewSize = getOptimalPreviewSize(camParams.getSupportedPreviewSizes());
            deviceInfo.setPreviewSize(previewSize);

            // preview size와 가장 근접한 picture size를 찾고 그것을 picturesize로 설정한다.
            double mRatio = (double)previewSize.width / (double)previewSize.height;
            Camera.Size pictureSize = previewSize;
            for (Camera.Size size : camParams.getSupportedPictureSizes()) {
                if (size.width <= previewSize.width && size.height <= previewSize.height && mRatio == (double)size.width/(double)size.height) {
                    if(size.height > 720) continue;
                    pictureSize = size;
                    break;
                }
            }
//            camParams.setPictureSize(pictureSize.width, pictureSize.height);
            camParams.setPictureSize(pictureSize.width, pictureSize.height);
            deviceInfo.setPictureSize(pictureSize);
            mCamera.setParameters(camParams);
            Log.i(TAG, "picturesize width: "+pictureSize.width + "");
            Log.i(TAG, "picturesize height: "+pictureSize.height + "");

            // 카메라 프리뷰 회전을 세로로 고정하고
            // 카메라 회전 매개 변수를 설정해서 이미지를 저장할 때 회전되서 저장되도록 함
            setCameraDisplayOrientation(CameraActivity.getInstance, CAMERA_FACING, mCamera);
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
            // 새로 변경된 설정으로 프리뷰를 시작한다

            camParams.setPreviewSize(previewSize.width, previewSize.height);
            mCamera.setParameters(camParams);

            Log.i(TAG, "PreviewSizes width: " + previewSize.width + "");
            Log.i(TAG, "PreviewSizes height: " + previewSize.height + "");

            setCameraDisplayOrientation(CameraActivity.getInstance, CAMERA_FACING, mCamera);

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
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }

        if(mCamera!=null) Log.i("life-cycle","surfaceDestroyed - "+mCamera.toString());
        else Log.i("life-cycle","surfaceDestroyed - Camera dead");
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

    public static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes)
    {
        final double ASPECT_TOLERANCE = 0.05;
        if( sizes == null )
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        Point display_size = new Point();

        Display display = CameraActivity.getInstance.getWindowManager().getDefaultDisplay();

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            display.getRealSize(display_size);
//        } else {
//            display.getSize(display_size);
//        }
        display.getSize(display_size);
        Log.d("CameraPreview_Log", "display_size: " + display_size.x + " x " + display_size.y);

        double targetRatio = ((double)mCamera.getParameters().getPictureSize().width) / (double)mCamera.getParameters().getPictureSize().height;
        Log.d("CameraPreview_Log","targetRatio: "+targetRatio);
        int targetHeight = Math.min(display_size.y, display_size.x);
        Log.d("CameraPreview_Log","targetHeight: "+targetHeight);
        if( targetHeight <= 0 ) {
            targetHeight = display_size.y;
        }
        // Try to find the size which matches the aspect ratio, and is closest match to display height
        for(Camera.Size size : sizes)
        {
            Log.d("CameraPreview_Log", "    supported preview size: " + size.width + ", " + size.height);
            double ratio = (double)size.width / size.height;
            if( Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE )
                continue;
            if( Math.abs(size.height - targetHeight) < minDiff ) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        if( optimalSize == null )
        {
            // can't find match for aspect ratio, so find closest one
            Log.d("CameraPreview_Log", "no preview size matches the aspect ratio");
//            optimalSize = getClosestSize(sizes, targetRatio);
            // Todo : optimal한 사이즈가 없는 경우에 처리
        }

        Log.d("CameraPreview_Log", "chose optimalSize: " + optimalSize.width + " x " + optimalSize.height);
        Log.d("CameraPreview_Log", "optimalSize ratio: " + ((double)optimalSize.width / optimalSize.height));
        return optimalSize;
    }
}
