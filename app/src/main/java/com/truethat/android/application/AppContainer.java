package com.truethat.android.application;

import com.truethat.android.application.auth.AuthManager;
import com.truethat.android.application.permissions.PermissionsManager;
import com.truethat.android.application.storage.internal.InternalStorageManager;
import com.truethat.android.empathy.ReactionDetectionManager;

/**
 * Proudly created by ohad on 08/08/2017 for TrueThat.
 */

public class AppContainer {
  private static AuthManager sAuthManager;
  private static DeviceManager sDeviceManager;
  private static InternalStorageManager sInternalStorageManager;
  private static PermissionsManager sPermissionsManager;
  private static ReactionDetectionManager sReactionDetectionManager;

  public static AuthManager getAuthManager() {
    return sAuthManager;
  }

  public static void setAuthManager(AuthManager authManager) {
    sAuthManager = authManager;
  }

  public static DeviceManager getDeviceManager() {
    return sDeviceManager;
  }

  public static void setDeviceManager(DeviceManager deviceManager) {
    sDeviceManager = deviceManager;
  }

  static InternalStorageManager getInternalStorageManager() {
    return sInternalStorageManager;
  }

  public static void setInternalStorageManager(InternalStorageManager internalStorageManager) {
    sInternalStorageManager = internalStorageManager;
  }

  public static PermissionsManager getPermissionsManager() {
    return sPermissionsManager;
  }

  public static void setPermissionsManager(PermissionsManager permissionsManager) {
    sPermissionsManager = permissionsManager;
  }

  public static ReactionDetectionManager getReactionDetectionManager() {
    return sReactionDetectionManager;
  }

  public static void setReactionDetectionManager(
      ReactionDetectionManager reactionDetectionManager) {
    sReactionDetectionManager = reactionDetectionManager;
  }
}
