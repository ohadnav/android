package com.truethat.android.ui.common;

import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import com.truethat.android.R;

@VisibleForTesting public class TestActivity extends BaseActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    // Should not authenticate on tests.
    mSkipAuth = true;
    super.onCreate(savedInstanceState);
  }

  @Override protected int getLayoutResId() {
    return R.layout.activity_test;
  }
}
