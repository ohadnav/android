package com.truethat.android.common;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.truethat.android.R;

public class NoCameraPermissionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_camera_permission);
    }
}
