package com.example.guideline_on_camera.util;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

public class ProgressMaterialDialog extends MaterialDialog {

    protected ProgressMaterialDialog(Builder builder) {
        super(builder);
    }

    public final static class Builder extends MaterialDialog.Builder {
        public Builder(final Context context) {
            super(context);
            progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .cancelable(false);
        }
    }
}
