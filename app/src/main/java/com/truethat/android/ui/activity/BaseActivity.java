package com.truethat.android.ui.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.application.TrueThatApp;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.common.util.RequestCodes;
import java.io.IOException;
import javax.inject.Inject;
import retrofit2.Retrofit;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.truethat.android.ui.activity.OnBoardingActivity.USER_NAME_INTENT;

/**
 * Proudly created by ohad on 13/06/2017 for TrueThat.
 */

public abstract class BaseActivity extends AppCompatActivity {
  /**
   * Logging tag. Assigned per implementing class in {@link #onCreate(Bundle)}.
   */
  protected String TAG = this.getClass().getSimpleName();
  /**
   * Whether to skip authentication.
   */
  protected boolean mSkipAuth = false;
  @BindView(R.id.activityRootView) protected View mRootView;

  @Inject Retrofit mRetrofit;

  /**
   * Permission not granted callback.
   *
   * @param permission the rejected permission.
   */
  @MainThread public void onRequestPermissionsFailed(Permission permission) {
    Intent askForPermission = new Intent(this, AskForPermissionActivity.class);
    askForPermission.putExtra(AskForPermissionActivity.PERMISSION_EXTRA, permission);
    startActivityForResult(askForPermission, permission.getRequestCode());
  }

  /**
   * Authentication success callback.
   */
  @MainThread public void onAuthOk() {

  }

  /**
   * Authentication failure callback.
   */
  @MainThread public void onAuthFailed() {
    Log.v(TAG, "Auth failed. Something smells bad...");
    Intent authFailed = new Intent(this, WelcomeActivity.class);
    authFailed.putExtra(WelcomeActivity.AUTH_FAILED, true);
    startActivity(authFailed);
  }

  /**
   * Called to initiate user on boarding, i.e. a new account creation.
   */
  @MainThread public void onBoarding() {
    Log.v(TAG, "New user, yay!");
    startActivityForResult(new Intent(this, OnBoardingActivity.class), RequestCodes.ON_BOARDING);
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    // Injects dependencies.
    ((TrueThatApp) getApplication()).getInjector(this);
    super.onCreate(savedInstanceState);
    // Disable landscape orientation.
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    // Initializes the view.
    setContentView(getLayoutResId());
    ButterKnife.bind(this);
    // Hide both the navigation bar and the status bar.
    View decorView = getWindow().getDecorView();
    int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_IMMERSIVE
        | View.SYSTEM_UI_FLAG_FULLSCREEN;
    decorView.setSystemUiVisibility(uiOptions);
  }

  /**
   * @return The activity layout resource ID, as found in {@link R.layout}.
   */
  protected abstract int getLayoutResId();

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RequestCodes.ON_BOARDING) {
      boolean authFailed = false;
      String newUserName = data.getExtras().getString(USER_NAME_INTENT);
      if (resultCode == RESULT_OK) {
        if (newUserName != null) {
          try {
            App.getAuthModule().getUser().updateNames(newUserName, this);
            App.getAuthModule().auth(this);
          } catch (IOException e) {
            Log.e(TAG, "Failed to update names.", e);
            e.printStackTrace();
            authFailed = true;
          }
        } else {
          // New names are null
          authFailed = true;
        }
      } else {
        // Result not OK
        authFailed = true;
      }
      if (authFailed) {
        runOnUiThread(new Runnable() {
          @Override public void run() {
            onAuthFailed();
          }
        });
      }
    }
  }

  @Override protected void onResume() {
    super.onResume();
    if (!mSkipAuth) {
      App.getAuthModule().auth(this);
    }
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    for (int i = 0; i < permissions.length; i++) {
      Permission permission = Permission.fromManifest(permissions[i]);
      if (grantResults[i] != PERMISSION_GRANTED) {
        Log.w(TAG, permission.name() + " not granted.");
        onRequestPermissionsFailed(permission);
      }
    }
  }

  protected <T> T createApiInterface(final Class<T> service) {
    return mRetrofit.create(service);
  }

  @Inject void logInjection() {
    Log.v(TAG, "Injecting " + getClass().getSimpleName() + " for " + getApplication().getClass()
        .getSimpleName() + "(" + getApplication().hashCode() + ")");
  }
}
