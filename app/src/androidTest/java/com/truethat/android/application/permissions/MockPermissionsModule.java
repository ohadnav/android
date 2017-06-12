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
  // Indicates whether rationale should be shown if not explicitly set otherwise.
  private static final boolean DEFAULT_RATIONALE_BEHAVIOUR = false;
  // Maps permission types to booleans indicating whether the permission was granted.
  private Map<Permission, Boolean> mPermissionToIsGranted = new HashMap<>();
  // Maps permission types to booleans indicating whether rationale should be shown. By default
  // the behaviour should match DEFAULT_RATIONALE_BEHAVIOUR.
  private Map<Permission, Boolean> mPermissionToShouldShowRationale = new HashMap<>();

  public MockPermissionsModule(Permission... grantedPermissions) {
    for (Permission permission : grantedPermissions) {
      grant(permission);
    }
  }

  @Override public boolean isPermissionGranted(@Nullable Context context, Permission permission) {
    return mPermissionToIsGranted.containsKey(permission) && mPermissionToIsGranted.get(permission);
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

  @Override public void requestIfNeeded(@Nullable Activity activity, Permission permission) {
    // If permission was already granted, return.
    if (isPermissionGranted(activity, permission)) return;
    // If permission was not already set, then grant it.
    if (!mPermissionToIsGranted.containsKey(permission)) {
      // Grant permission.
      grant(permission);
      // Grants it for real, if needed.
      try {
        if (activity != null && !DEFAULT_PERMISSIONS_MODULE.isPermissionGranted(activity,
            permission)) {
          PermissionsTestUtil.grantPermission(Permission.CAMERA);
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

  public void revokeAndForbid(Permission permission) {
    mPermissionToIsGranted.put(permission, false);
  }

  public void grant(Permission permission) {
    mPermissionToIsGranted.put(permission, true);
  }

  public void setExplicitRationaleBehaviour(Permission permission, boolean rationaleBehaviour) {
    mPermissionToShouldShowRationale.put(permission, rationaleBehaviour);
  }
}
