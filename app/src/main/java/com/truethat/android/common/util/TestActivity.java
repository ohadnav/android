package com.truethat.android.common.util;

import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import com.truethat.android.R;
import com.truethat.android.common.BaseActivity;

@VisibleForTesting public class TestActivity extends BaseActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);
  }
}
