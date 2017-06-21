package com.truethat.android.application.permissions;

import android.app.Activity;
import android.content.Context;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */

public interface PermissionsModule {
  /**
   * @param context for the desired permission
   * @param permission for which to enquire
   * @return whether the permission is granted.
   */
  boolean isPermissionGranted(Context context, Permission permission);

  /**
   * @param activity to provide context
   * @param permission for which to enquire
   * @return whether an explanatory dialogue should be shown to the use prior to asking for
   * permission.
   */
  @SuppressWarnings("unused") boolean shouldShowRationale(Activity activity, Permission permission);

  /**
   * Checks whether the permission was already granted, and if it was not, then it requests.
   *
   * @param activity to provide context
   * @param permission for which to enquire
   */
  void requestIfNeeded(Activity activity, Permission permission);
}
