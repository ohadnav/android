package com.truethat.android.common;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.truethat.android.application.App;
import com.truethat.android.application.permissions.AskForPermissionActivity;
import com.truethat.android.application.permissions.Permission;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Proudly created by ohad on 13/06/2017 for TrueThat.
 */

public class BaseActivity extends AppCompatActivity {
  /**
   * Logging tag. Assigned per implementing class in {@link #onCreate(Bundle)}.
   */
  protected String TAG;
  /**
   * Whether to skip authentication.
   */
  protected boolean mSkipAuth = false;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    TAG = this.getClass().getSimpleName();
    super.onCreate(savedInstanceState);
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

  public void onRequestPermissionsFailed(Permission permission) {
    Intent askForPermission = new Intent(this, AskForPermissionActivity.class);
    askForPermission.putExtra(AskForPermissionActivity.PERMISSION_EXTRA, permission);
    startActivityForResult(askForPermission, permission.getRequestCode());
  }
}
