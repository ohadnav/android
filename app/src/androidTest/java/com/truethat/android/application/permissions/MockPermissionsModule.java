package com.truethat.android.application.permissions;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */

@SuppressWarnings("WeakerAccess") public class MockPermissionsModule implements PermissionsModule {
  private static final DefaultPermissionsModule DEFAULT_PERMISSIONS_MODULE =
      new DefaultPermissionsModule();
  /**
   * Indicates whether rationale should be shown if not explicitly set otherwise.
   */
  private static final boolean DEFAULT_RATIONALE_BEHAVIOUR = false;
  /**
   * Maps permission types to booleans indicating whether the permission was granted.
   */
  private Map<Permission, PermissionState> mPermissionToState = new HashMap<>();
  /**
   * Maps permission types to booleans indicating whether rationale should be shown. By default
   * the behaviour should match DEFAULT_RATIONALE_BEHAVIOUR.
   */
  private Map<Permission, Boolean> mPermissionToShouldShowRationale = new HashMap<>();

  public MockPermissionsModule(Permission... grantedPermissions) {
    for (Permission permission : grantedPermissions) {
      grant(permission);
    }
  }

  @Override public boolean isPermissionGranted(@Nullable Context context, Permission permission) {
    return mPermissionToState.containsKey(permission)
        && mPermissionToState.get(permission) == PermissionState.GRANTED;
  }

  @Override public boolean shouldShowRationale(Activity activity, Permission permission) {
    // If permission was already granted then return false.
    if (isPermissionGranted(activity, permission)) return false;
    // Behaviour has been explicitly set
    if (mPermissionToShouldShowRationale.containsKey(permission)) {
      return mPermissionToShouldShowRationale.get(permission);
    }
    return DEFAULT_RATIONALE_BEHAVIOUR;
  }

  /**
   * When permissions are not forbid they are always granted, this is useful to mock the flow in
   * which a user allows a permission.
   */
  @Override public void requestIfNeeded(@Nullable Activity activity, Permission permission) {
    // If permission was already granted, return.
    if (isPermissionGranted(activity, permission)) return;
    // If permission was not already set, then grant it.
    if (!mPermissionToState.containsKey(permission)
        || mPermissionToState.get(permission) != PermissionState.FORBID) {
      // Grant permission.
      grant(permission);
      // Grants it for real, if needed.
      try {
        if (activity != null && !DEFAULT_PERMISSIONS_MODULE.isPermissionGranted(activity,
            permission)) {
          PermissionsTestUtil.grantPermission(permission);
        }
      } catch (Exception ignored) {
      }
    }
    // Invoke permission request callback.
    if (activity != null) {
      activity.onRequestPermissionsResult(permission.getRequestCode(),
          new String[] { permission.getManifest() }, new int[] {
              isPermissionGranted(activity, permission) ? PackageManager.PERMISSION_GRANTED
                  : PackageManager.PERMISSION_DENIED
          });
    }
  }

  public void forbid(Permission permission) {
    mPermissionToState.put(permission, PermissionState.FORBID);
  }

  public void grant(Permission permission) {
    mPermissionToState.put(permission, PermissionState.GRANTED);
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

  public void setExplicitRationaleBehaviour(Permission permission, boolean rationaleBehaviour) {
    mPermissionToShouldShowRationale.put(permission, rationaleBehaviour);
  }

  private enum PermissionState {
    GRANTED, FORBID
  }
}
