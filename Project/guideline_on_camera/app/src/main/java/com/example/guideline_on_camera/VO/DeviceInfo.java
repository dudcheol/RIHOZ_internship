package com.example.guideline_on_camera.VO;

import android.hardware.Camera;

import java.util.List;

public class DeviceInfo {
    private Camera.Size previewSize;
    private Camera.Size pictureSize;
    private List<String> hasFocusMode;
    private boolean autoFocusable;

    /* getter */
    public Camera.Size getPreviewSize() {
        return previewSize;
    }

    public Camera.Size getPictureSize() {
        return pictureSize;
    }

    public List<String> getHasFocusMode() {
        return hasFocusMode;
    }

    public boolean isAutoFocusable() {
        return autoFocusable;
    }

    /* setter */
    public void setPreviewSize(Camera.Size previewSize) {
        this.previewSize = previewSize;
    }

    public void setPictureSize(Camera.Size pictureSize) {
        this.pictureSize = pictureSize;
    }

    public void setHasFocusMode(List<String> hasFocusMode) {
        this.hasFocusMode = hasFocusMode;
    }

    public void setAutoFocusable(boolean autoFocusable) {
        this.autoFocusable = autoFocusable;
    }
}
