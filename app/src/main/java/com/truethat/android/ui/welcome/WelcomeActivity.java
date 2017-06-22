package com.truethat.android.ui.welcome;

import android.os.Bundle;
import android.view.View;
import butterknife.OnClick;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.ui.common.BaseActivity;

public class WelcomeActivity extends BaseActivity {
  public static final String AUTH_FAILED = "authFailed";

  @Override protected void onCreate(Bundle savedInstanceState) {
    mSkipAuth = true;
    super.onCreate(savedInstanceState);
  }

  @Override protected int getLayoutResId() {
    return R.layout.activity_welcome;
  }

  @Override protected void onResume() {
    super.onResume();
    // If the user is initialized, then finish activity.
    if (App.getAuthModule().isAuthOk()) {
      finish();
    }
    if (getIntent().getExtras().getBoolean(AUTH_FAILED)) {
      onAuthFailed();
    }
  }

  @Override public void onAuthFailed() {
    // Display error message to the user.
    findViewById(R.id.errorText).setVisibility(View.VISIBLE);
    // Display sign in message
    findViewById(R.id.signInText).setVisibility(View.VISIBLE);
  }

  @Override public void onAuthOk() {
    finish();
  }

  /**
   * Auth that is initiated by {@link R.id#signInText}.
   */
  public void userInitiatedAuth(View view) {
    App.getAuthModule().auth(this);
  }

  @OnClick(R.id.joinLayout) public void doOnBoarding() {
    super.onBoarding();
  }
}
