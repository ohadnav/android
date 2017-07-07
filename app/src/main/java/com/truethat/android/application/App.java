package com.truethat.android.application;

import android.support.annotation.VisibleForTesting;
import com.truethat.android.application.auth.AuthModule;
import com.truethat.android.application.auth.DefaultAuthModule;
import com.truethat.android.application.permissions.DefaultPermissionsModule;
import com.truethat.android.application.permissions.PermissionsModule;
import com.truethat.android.application.storage.internal.DefaultInternalStorage;
import com.truethat.android.application.storage.internal.InternalStorage;
import com.truethat.android.empathy.DefaultReactionDetectionModule;
import com.truethat.android.empathy.NullEmotionDetectionClassifier;
import com.truethat.android.empathy.ReactionDetectionModule;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */

public class App {
  private static PermissionsModule sPermissionsModule = new DefaultPermissionsModule();
  private static InternalStorage sInternalStorage = new DefaultInternalStorage();
  private static AuthModule sAuthModule = new DefaultAuthModule();
  private static ReactionDetectionModule sReactionDetectionModule =
      new DefaultReactionDetectionModule(new NullEmotionDetectionClassifier());
  private static DeviceManager sDeviceManager = new DefaultDeviceManager();

  public static PermissionsModule getPermissionsModule() {
    return sPermissionsModule;
  }

  @VisibleForTesting public static void setPermissionsModule(PermissionsModule permissionsModule) {
    sPermissionsModule = permissionsModule;
  }

  public static InternalStorage getInternalStorage() {
    return sInternalStorage;
  }

  @VisibleForTesting public static void setInternalStorage(InternalStorage internalStorage) {
    sInternalStorage = internalStorage;
  }

  public static AuthModule getAuthModule() {
    return sAuthModule;
  }

  @VisibleForTesting public static void setAuthModule(AuthModule authModule) {
    sAuthModule = authModule;
  }

  public static ReactionDetectionModule getReactionDetectionModule() {
    return sReactionDetectionModule;
  }

  @VisibleForTesting
  public static void setReactionDetectionModule(ReactionDetectionModule reactionDetectionModule) {
    sReactionDetectionModule = reactionDetectionModule;
  }

  public static DeviceManager getDeviceManager() {
    return sDeviceManager;
  }

  @VisibleForTesting public static void setDeviceManager(DeviceManager deviceManager) {
    sDeviceManager = deviceManager;
  }
}
