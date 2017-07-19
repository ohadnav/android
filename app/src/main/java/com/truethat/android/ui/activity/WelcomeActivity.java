package com.truethat.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import butterknife.OnClick;
import com.truethat.android.R;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.common.util.RequestCodes;

public class WelcomeActivity extends BaseActivity {
  public static final String AUTH_FAILED = "authFailed";

  @Override public void onAuthOk() {
    finish();
  }

  @Override public void onAuthFailed() {
    // Display error message to the user.
    findViewById(R.id.errorText).setVisibility(View.VISIBLE);
  }

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
    if (mAuthManager.isAuthOk()) {
      finish();
    }
    if (getIntent().getExtras().getBoolean(AUTH_FAILED)) {
      onAuthFailed();
    }
  }

  /**
   * Auth that is initiated by the user.
   */
  @OnClick(R.id.signInText) public void userInitiatedAuth(View view) {
    mAuthManager.signIn(this);
  }

  /**
   * Called to initiate user on boarding, i.e. a new account creation.
   */
  @OnClick(R.id.joinLayout) public void onBoarding() {
    Log.v(TAG, "New user, yay!");
    mPermissionsManager.requestIfNeeded(this, Permission.PHONE);
    if (!mPermissionsManager.isPermissionGranted(Permission.PHONE)) {
      Log.i(TAG, "No phone permission, stopping on boarding.");
      // No phone permission, stop here, to let ask for permission activity gain control.
      return;
    }
    runOnUiThread(new Runnable() {
      @Override public void run() {
        startActivityForResult(new Intent(WelcomeActivity.this, OnBoardingActivity.class),
            RequestCodes.ON_BOARDING);
      }
    });
  }
}
