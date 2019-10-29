package com.example.guideline_on_camera.VO;

import android.hardware.Camera;

public class DeviceInfo {
    private int screenWidth;
    private int screenHeight;
    private Camera.Size previewSize;
    private Camera.Size pictureSize;

    /* getter */
    public Camera.Size getPreviewSize() {
        return previewSize;
    }

    public Camera.Size getPictureSize() {
        return pictureSize;
    }


    /* setter */
    public void setPreviewSize(Camera.Size previewSize) {
        this.previewSize = previewSize;
    }

    public void setPictureSize(Camera.Size pictureSize) {
        this.pictureSize = pictureSize;
    }
}
