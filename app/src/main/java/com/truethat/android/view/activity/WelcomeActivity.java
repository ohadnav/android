package com.truethat.android.view.activity;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import butterknife.OnClick;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.application.auth.AuthListener;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.common.util.RequestCodes;
import com.truethat.android.databinding.ActivityWelcomeBinding;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

public class WelcomeActivity extends
    BaseActivity<BaseViewInterface, BaseViewModel<BaseViewInterface>, ActivityWelcomeBinding>
    implements AuthListener {
  /**
   * Records the user last click target view ID. Otherwise, duplicate clicks are needed when asking
   * for permission.
   */
  private int mLastClick = 0;

  @Override public void onPermissionGranted(Permission permission) {
    super.onPermissionGranted(permission);
    if (mLastClick == R.id.signInText) {
      userInitiatedAuth();
    } else if (mLastClick == R.id.joinLayout) {
      onBoarding();
    }
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_welcome, this);
  }

  @Override public void onResume() {
    super.onResume();
    // If the user is authenticated, then finish activity.
    if (AppContainer.getAuthManager().isAuthOk()) {
      startActivity(new Intent(this, MainActivity.class));
      finish();
    }
  }

  @Override public void onAuthOk() {
    // If the user is authenticated, then finish activity.
    startActivity(new Intent(this, MainActivity.class));
    finish();
  }

  @Override public void onAuthFailed() {
    Log.v(TAG, "Auth failed. Something smells bad...");
    // Display error message to the user.
    findViewById(R.id.errorText).setVisibility(View.VISIBLE);
  }

  /**
   * Auth that is initiated by the user.
   */
  @OnClick(R.id.signInText) public void userInitiatedAuth() {
    mLastClick = R.id.signInText;
    AppContainer.getPermissionsManager().requestIfNeeded(this, Permission.PHONE);
    if (!AppContainer.getPermissionsManager().isPermissionGranted(Permission.PHONE)) {
      Log.i(TAG, "No phone permission, stopping sign in.");
      // No phone permission, stop here, to let ask for permission activity gain control.
      return;
    }
    // Reset last click.
    mLastClick = 0;
    AppContainer.getAuthManager().signIn(this);
  }

  /**
   * Called to initiate user on boarding, i.e. a new account creation.
   */
  @OnClick(R.id.joinLayout) public void onBoarding() {
    mLastClick = R.id.joinLayout;
    Log.v(TAG, "New user, yay!");
    AppContainer.getPermissionsManager().requestIfNeeded(this, Permission.PHONE);
    if (!AppContainer.getPermissionsManager().isPermissionGranted(Permission.PHONE)) {
      Log.i(TAG, "No phone permission, stopping on boarding.");
      // No phone permission, stop here, to let ask for permission activity gain control.
      return;
    }
    // Reset last click.
    mLastClick = 0;
    runOnUiThread(new Runnable() {
      @Override public void run() {
        startActivityForResult(new Intent(WelcomeActivity.this, OnBoardingActivity.class),
            RequestCodes.ON_BOARDING);
      }
    });
  }
}
