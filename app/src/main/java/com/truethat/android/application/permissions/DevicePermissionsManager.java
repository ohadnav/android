package com.truethat.android.application.permissions;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */

public class DevicePermissionsManager implements PermissionsManager {
  private Context mContext;

  public DevicePermissionsManager(Context context) {
    mContext = context;
  }

  @Override public boolean isPermissionGranted(Permission permission) {
    return ActivityCompat.checkSelfPermission(mContext, permission.getManifest())
        == PackageManager.PERMISSION_GRANTED;
  }

  @Override public void requestIfNeeded(Activity activity, Permission permission) {
    // If permission was already granted, then return.
    if (isPermissionGranted(permission)) return;
    // Request permission.
    ActivityCompat.requestPermissions(activity, new String[] { permission.getManifest() },
        permission.getRequestCode());
  }
}
