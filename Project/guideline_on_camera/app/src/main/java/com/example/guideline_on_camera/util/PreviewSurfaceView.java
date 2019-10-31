package com.example.guideline_on_camera.util;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

import androidx.annotation.RequiresApi;

public class PreviewSurfaceView extends SurfaceView {
    private boolean listenerSet;
    private boolean drawingViewSet;
    private CameraPreview camPreview;
    private DrawingView drawingView;

    public PreviewSurfaceView(Context context) {
        super(context);
    }

    public PreviewSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreviewSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PreviewSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    // some code here

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("cameraFocus","PreviewSurfaceView onTouchEvent!");
        if (!listenerSet) {
            return false;
        }

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            float x = event.getX();
            float y = event.getY();

            Rect touchRect = new Rect(
                    (int)(x - 100),
                    (int)(y - 100),
                    (int)(x + 100),
                    (int)(y + 100));

            final Rect targetFocusRect = new Rect(
                    touchRect.left * 2000/this.getWidth() - 1000,
                    touchRect.top * 2000/this.getHeight() - 1000,
                    touchRect.right * 2000/this.getWidth() - 1000,
                    touchRect.bottom * 2000/this.getHeight() - 1000);

            camPreview.doTouchFocus(targetFocusRect);
            if (drawingViewSet) {
                drawingView.setHaveTouch(true, touchRect);
                drawingView.invalidate();

                // Remove the square indicator after 1000 msec
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        drawingView.setHaveTouch(false, new Rect(0,0,0,0));
                        drawingView.invalidate();
                    }
                }, 1000);
            }

        }

        return false;
    }

    /**
     * set CameraPreview instance for touch focus.
     * @param camPreview - CameraPreview
     */
    public void setListener(CameraPreview camPreview) {
        this.camPreview = camPreview;
        listenerSet = true;
    }

    /**
     * set DrawingView instance for touch focus indication.
     * @param dView - DrawingView
     */
    public void setDrawingView(DrawingView dView) {
        drawingView = dView;
        drawingViewSet = true;
    }
}