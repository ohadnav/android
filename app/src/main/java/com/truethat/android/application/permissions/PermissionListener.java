package com.truethat.android.application.permissions;

import com.truethat.android.viewmodel.viewinterface.BaseListener;

/**
 * Proudly created by ohad on 25/10/2017 for TrueThat.
 * <p>
 * Interface between activity and its subcomponents to communicate asking for permission result.
 */

public interface PermissionListener extends BaseListener {
  /**
   * Permission granted callback.
   *
   * @param permission that was just granted.
   */
  void onPermissionGranted(Permission permission);

  /**
   * Permission not granted callback.
   *
   * @param permission that was just rejected.
   */
  void onPermissionRejected(Permission permission);
}
