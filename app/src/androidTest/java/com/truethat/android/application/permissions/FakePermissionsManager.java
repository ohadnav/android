package com.truethat.android.application.permissions;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */

@SuppressWarnings("WeakerAccess") public class FakePermissionsManager
    implements PermissionsManager {
  /**
   * Maps permission types to booleans indicating whether the permission was granted.
   */
  private Map<Permission, PermissionState> mPermissionToState = new HashMap<>();
  /**
   * Whether to invoke request callback when requests are not already granted.
   */
  private boolean mInvokeRequestCallback = false;

  public FakePermissionsManager(Permission... grantedPermissions) {
    for (Permission permission : grantedPermissions) {
      grant(permission);
    }
  }

  public void grant(Permission permission) {
    mPermissionToState.put(permission, PermissionState.GRANTED);
  }

  @Override public boolean isPermissionGranted(Permission permission) {
    return mPermissionToState.containsKey(permission)
        && mPermissionToState.get(permission) == PermissionState.GRANTED;
  }

  /**
   * When permissions are not forbid they are always granted, this is useful to mock the flow in
   * which a user allows a permission.
   * <p>
   * Request callback is invoked in the following conditions:
   * <ul>
   * <li>{@code activity} is not null.</li>
   * <li>{@code permission} is not already granted.</li>
   * <li>{@code permission} is forbidden or {@code mInvokeRequestCallback} is true.</li>
   * </ul>
   */
  @Override public void requestIfNeeded(@Nullable Activity activity, Permission permission) {
    boolean activityNotNull = activity != null;
    boolean permissionNotAlreadyGranted =
        mPermissionToState.get(permission) != PermissionState.GRANTED;
    boolean permissionForbiddenOrFlagIsTrue =
        mInvokeRequestCallback || mPermissionToState.get(permission) == PermissionState.FORBID;
    boolean shouldInvokeRequestCallback =
        activityNotNull && permissionNotAlreadyGranted && permissionForbiddenOrFlagIsTrue;
    // If permission was not forbidden, then grant it.
    if (mPermissionToState.get(permission) != PermissionState.FORBID) {
      // Grant permission.
      grant(permission);
      // Grants it for real, if needed.
      try {
        PermissionsManager devicePermissionsManager = new DevicePermissionsManager(activity);
        if (activity != null && !devicePermissionsManager.isPermissionGranted(permission)) {
          PermissionsTestUtil.grantPermission(permission);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    // Invoke permission request callback.
    if (shouldInvokeRequestCallback) {
      activity.onRequestPermissionsResult(permission.getRequestCode(),
          new String[] { permission.getManifest() }, new int[] {
              isPermissionGranted(permission) ? PackageManager.PERMISSION_GRANTED
                  : PackageManager.PERMISSION_DENIED
          });
    }
  }

  public void forbid(Permission permission) {
    mPermissionToState.put(permission, PermissionState.FORBID);
  }

  /**
   * Resets {@link PermissionState} for {@code permission}. Usually, in order to prepare for a
   * natural flow of permission request.
   *
   * @param permission to prepare for {@link #requestIfNeeded(Activity, Permission)}
   */
  public void reset(Permission permission) {
    mPermissionToState.remove(permission);
  }

  public void setInvokeRequestCallback(boolean invokeRequestCallback) {
    mInvokeRequestCallback = invokeRequestCallback;
  }

  private enum PermissionState {
    GRANTED, FORBID
  }
}
