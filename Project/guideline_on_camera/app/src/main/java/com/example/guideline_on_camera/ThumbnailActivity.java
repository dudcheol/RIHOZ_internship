package com.example.guideline_on_camera;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

public class ThumbnailActivity extends AppCompatActivity {

    Button retryBtn, uploadBtn;
    ImageView thumbnail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thumbnail);

        initSetting();
        receiveData();

    }

    private void initSetting() {
        retryBtn = findViewById(R.id.retryBtn);
        uploadBtn = findViewById(R.id.uploadBtn);
        thumbnail = findViewById(R.id.thumbnail);
    }

    private void receiveData() {

    }
}
