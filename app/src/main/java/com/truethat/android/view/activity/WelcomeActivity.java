package com.truethat.android.view.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import butterknife.BindView;
import butterknife.OnClick;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.application.auth.AuthListener;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.common.util.RequestCodes;
import com.truethat.android.databinding.ActivityWelcomeBinding;
import com.truethat.android.model.Video;
import com.truethat.android.view.custom.BaseDialog;
import com.truethat.android.view.custom.StyledTextView;
import com.truethat.android.view.fragment.VideoFragment;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

public class WelcomeActivity extends
    BaseActivity<BaseViewInterface, BaseViewModel<BaseViewInterface>, ActivityWelcomeBinding>
    implements AuthListener {
  @BindView(R.id.welcome_signIn) StyledTextView mSignIn;
  @BindView(R.id.welcome_title) StyledTextView mTitle;
  /**
   * Records the user last click target view ID. Otherwise, duplicate clicks are needed when asking
   * for permission.
   */
  private int mLastClick = 0;
  private VideoFragment mVideoFragment;
  private Dialog mDialog;

  @Override public void onPermissionGranted(Permission permission) {
    super.onPermissionGranted(permission);
    if (mLastClick == R.id.welcome_signIn) {
      userInitiatedAuth();
    } else if (mLastClick == R.id.welcome_join) {
      onBoarding();
    }
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_welcome, this);
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mVideoFragment = VideoFragment.newInstance(new Video(R.raw.welcome));
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    fragmentTransaction.replace(R.id.welcome_videoLayout, mVideoFragment);
    mVideoFragment.setVisibilityListener(this);
    fragmentTransaction.commit();
  }

  @Override protected void onPause() {
    super.onPause();
    if (mDialog != null) {
      mDialog.dismiss();
      mDialog = null;
    }
  }

  @Override public void onResume() {
    super.onResume();
    // If the user is authenticated, then finish activity.
    if (AppContainer.getAuthManager().isAuthOk()) {
      startActivity(new Intent(this, MainActivity.class));
      finish();
    }
    // Send video to back
    mSignIn.bringToFront();
    mTitle.bringToFront();
  }

  @Override public void onAuthOk() {
    // Hides loading image
    mVideoFragment.getLoadingImage().setVisibility(View.GONE);
    // If the user is authenticated, then finish activity.
    startActivity(new Intent(this, MainActivity.class));
    finish();
  }

  @Override public void onAuthFailed() {
    Log.v(TAG, "Auth failed. Failed sign in?");
    // Hides loading image
    mVideoFragment.getLoadingImage().setVisibility(View.GONE);
    // Display error message to the user.
    showDialog();
  }

  /**
   * Auth that is initiated by the user.
   */
  @OnClick(R.id.welcome_signIn) public void userInitiatedAuth() {
    mLastClick = R.id.welcome_signIn;
    AppContainer.getPermissionsManager().requestIfNeeded(this, Permission.PHONE);
    if (!AppContainer.getPermissionsManager().isPermissionGranted(Permission.PHONE)) {
      Log.i(TAG, "No phone permission, stopping sign in.");
      // No phone permission, stop here, to let ask for permission activity gain control.
      return;
    }
    // Reset last click.
    mLastClick = 0;
    AppContainer.getAuthManager().signIn(this);
    // Shows loading image
    mVideoFragment.getLoadingImage().setVisibility(View.VISIBLE);
  }

  /**
   * Called to initiate user on boarding, i.e. a new account creation.
   */
  @OnClick(R.id.welcome_join) public void onBoarding() {
    mLastClick = R.id.welcome_join;
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

  private void showDialog() {
    if (mDialog != null) {
      mDialog.dismiss();
    }
    runOnUiThread(new Runnable() {
      @Override public void run() {
        mDialog = new BaseDialog(WelcomeActivity.this, R.string.welcome_dialog_title,
            R.string.welcome_dialog_message, R.string.welcome_dialog_button);
        mDialog.show();
      }
    });
  }
}
