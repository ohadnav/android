package com.truethat.android.view.activity;

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
import com.google.gson.Gson;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.application.DeviceManager;
import com.truethat.android.application.auth.AuthListener;
import com.truethat.android.application.auth.AuthManager;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.application.permissions.PermissionsManager;
import com.truethat.android.empathy.ReactionDetectionManager;
import javax.inject.Inject;
import retrofit2.Retrofit;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Proudly created by ohad on 13/06/2017 for TrueThat.
 */

public abstract class BaseActivity extends AppCompatActivity implements AuthListener {
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

  @Inject Gson mGson;

  @Inject PermissionsManager mPermissionsManager;

  @Inject AuthManager mAuthManager;

  @Inject DeviceManager mDeviceManager;

  @Inject ReactionDetectionManager mReactionDetectionManager;

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
  public void onAuthOk() {
    Log.v(TAG, "onAuthOk");
  }

  /**
   * Authentication failure callback.
   */
  public void onAuthFailed() {
    Log.v(TAG, "Auth failed. Something smells bad...");
    runOnUiThread(new Runnable() {
      @Override public void run() {
        Intent authFailed = new Intent(BaseActivity.this, WelcomeActivity.class);
        authFailed.putExtra(WelcomeActivity.AUTH_FAILED, true);
        startActivity(authFailed);
      }
    });

  }

  public <T> T createApiInterface(final Class<T> service) {
    return mRetrofit.create(service);
  }

  public App getApp() {
    return (App) getApplication();
  }

  public ReactionDetectionManager getReactionDetectionManager() {
    return mReactionDetectionManager;
  }

  public PermissionsManager getPermissionsManager() {
    return mPermissionsManager;
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    // Injects dependencies.
    getApp().inject(this);
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

  @Override protected void onResume() {
    super.onResume();
    if (!mSkipAuth) {
      mAuthManager.auth(this);
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

  @Inject void logInjection() {
    Log.v(TAG, "Injecting " + getClass().getSimpleName() + " for " + getApplication().getClass()
        .getSimpleName() + "(" + getApplication().hashCode() + ")");
  }
}
