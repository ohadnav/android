package com.truethat.android.application.permissions;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */

public class DefaultPermissionsModule implements PermissionsModule {
  @Override public boolean isPermissionGranted(Context context, Permission permission) {
    return ActivityCompat.checkSelfPermission(context, permission.getManifest()) == PackageManager.PERMISSION_GRANTED;
  }

  @Override public boolean shouldShowRationale(Activity activity, Permission permission) {
    return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission.getManifest());
  }

  @Override public void requestIfNeeded(Activity activity, Permission permission) {
    // If permission was already granted, then return.
    if (isPermissionGranted(activity, permission)) return;
    // Request permission.
    ActivityCompat.requestPermissions(activity, new String[] { permission.getManifest() }, permission.getRequestCode());
  }
}
