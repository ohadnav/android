package com.truethat.android.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import butterknife.OnClick;
import com.truethat.android.R;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.common.util.RequestCodes;
import com.truethat.android.databinding.ActivityWelcomeBinding;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

public class WelcomeActivity extends
    BaseActivity<BaseViewInterface, BaseViewModel<BaseViewInterface>, ActivityWelcomeBinding> {
  public static final String EXTRA_AUTH_FAILED = "authFailed";

  @Override public void onAuthOk() {
    super.onAuthOk();
    finish();
  }

  @Override public void onAuthFailed() {
    // Display error message to the user.
    findViewById(R.id.errorText).setVisibility(View.VISIBLE);
  }

  @Override public void onDestroy() {
    super.onDestroy();
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_welcome, this);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    mSkipAuth = true;
    super.onCreate(savedInstanceState);
  }

  @Override public void onResume() {
    super.onResume();
    // If the user is initialized, then finish activity.
    if (mAuthManager.isAuthOk()) {
      finish();
    }
    if (getIntent().getExtras().getBoolean(EXTRA_AUTH_FAILED)) {
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
