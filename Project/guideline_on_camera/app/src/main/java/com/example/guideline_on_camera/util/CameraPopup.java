package com.example.guideline_on_camera.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.guideline_on_camera.CameraActivity;
import com.example.guideline_on_camera.R;

public class CameraPopup extends Activity {
    private TextView Title, FirstDescription, SecondDescription;

    private String serviceCode;
    private String[] popupContents;

    private Button okBtn;

    private String stringTitle_ex,
            firstDescription_ex,
            secondDescription_ex,
            okBtn_ex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera_popup);
        setFinishOnTouchOutside(false);

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = (int) (display.getWidth());
        getWindow().getAttributes().width = width;
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        init();
    }

    @SuppressLint("ResourceAsColor")
    private void init() {
        Intent intent = getIntent();
        serviceCode = intent.getStringExtra("serviceCode");
        popupContents = intent.getStringArrayExtra("popupContents");
        if (TextUtils.isEmpty(serviceCode)) {
            serviceCode = "";
        }
        if(popupContents!=null) {
            for(int i=0;i<popupContents.length;i++){
                if(TextUtils.isEmpty(popupContents[i])) popupContents[i] = "";
            }
        }

        Title = (TextView) findViewById(R.id.stringTitle);
        FirstDescription = (TextView) findViewById(R.id.stringFirstDescription);
        SecondDescription = (TextView) findViewById(R.id.stringSecondDescription);
        okBtn = (Button) findViewById(R.id.okBtn);

        View.OnClickListener okBtnListener = null;

        switch (serviceCode) {
            case "networkErr":
                stringTitle_ex = "인터넷 연결을 확인해주세요";
                firstDescription_ex = "사진 저장을 위해 핸드폰과 인터넷이";
                secondDescription_ex = "잘 연결되어있는지 확인해주세요";
                okBtn_ex = "확인";
                okBtn.setTextColor(getResources().getColor(R.color.text_black_1));
                okBtn.setBackgroundResource(R.drawable.border_gold);
                break;
            case "firstTryFail":
                stringTitle_ex = popupContents[0];
                firstDescription_ex = popupContents[1];
                secondDescription_ex = popupContents[2];
                okBtn_ex = popupContents[3];
                okBtn.setTextColor(getResources().getColor(R.color.text_black_1));
                okBtn.setBackgroundResource(R.drawable.border_gold);
                break;
            case "secondTryFail":
                stringTitle_ex = popupContents[0];
                firstDescription_ex = popupContents[1];
                secondDescription_ex = popupContents[2];
                okBtn_ex = popupContents[3];
                okBtn.setTextColor(getResources().getColor(R.color.text_gold));
                okBtn.setBackgroundResource(R.drawable.border_black_corners_fill);
                break;
            default:
                break;
        }
        Title.setText(stringTitle_ex);
        FirstDescription.setText(firstDescription_ex);
        SecondDescription.setText(secondDescription_ex);
        okBtn.setText(okBtn_ex);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.okBtn:
                if(serviceCode.equals("secondTryFail")){
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(getString(R.string.call_center_number_scheme))));
                    CameraActivity.getInstance.finish();
                }
                this.finish();
                break;
        }
    }
}
