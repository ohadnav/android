package com.truethat.android.ui.activity;

import android.media.Image;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import com.truethat.android.R;
import com.truethat.android.ui.common.camera.CameraFragment;

@VisibleForTesting public class TestActivity extends BaseActivity
    implements CameraFragment.OnPictureTakenListener {

  @Override public void processImage(Image image) {
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
