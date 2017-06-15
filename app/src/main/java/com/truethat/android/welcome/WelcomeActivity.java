package com.truethat.android.welcome;

import android.os.Bundle;
import com.truethat.android.R;
import com.truethat.android.common.BaseActivity;

public class WelcomeActivity extends BaseActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    mSkipAuth = true;
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_welcome);
  }
}
