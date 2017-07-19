package com.truethat.android.application.permissions;

import android.app.Activity;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */

public interface PermissionsManager {
  /**
   * @param permission for which to enquire
   *
   * @return whether the permission is granted.
   */
  boolean isPermissionGranted(Permission permission);

  /**
   * Checks whether the permission was already granted, and if it was not, then it requests.
   *
   * @param activity   to provide mContext
   * @param permission for which to enquire
   */
  void requestIfNeeded(Activity activity, Permission permission);
}
