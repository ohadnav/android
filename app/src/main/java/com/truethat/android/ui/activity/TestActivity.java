package com.truethat.android.ui.activity;

import android.media.Image;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import com.google.gson.Gson;
import com.truethat.android.R;
import com.truethat.android.application.DeviceManager;
import com.truethat.android.application.auth.AuthManager;
import com.truethat.android.ui.common.camera.CameraFragment;

@VisibleForTesting public class TestActivity extends BaseActivity
    implements CameraFragment.OnPictureTakenListener {

  @Override public void processImage(Image image) {
  }

  public AuthManager getAuthManager() {
    return mAuthManager;
  }

  public DeviceManager getDeviceManager() {
    return mDeviceManager;
  }

  public Gson getGson() {
    return mGson;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    // Should not authenticate on tests.
    mSkipAuth = true;
    super.onCreate(savedInstanceState);
  }

  @Override protected int getLayoutResId() {
    return R.layout.activity_test;
  }
}
