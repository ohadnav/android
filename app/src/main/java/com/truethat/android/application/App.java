package com.truethat.android.application;

import android.app.Application;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.truethat.android.BuildConfig;
import com.truethat.android.application.auth.BaseAuthManager;
import com.truethat.android.application.permissions.DevicePermissionsManager;
import com.truethat.android.application.storage.internal.DeviceInternalStorageManager;
import com.truethat.android.empathy.AffectivaReactionDetectionManager;
import io.fabric.sdk.android.Fabric;

/**
 * Proudly created by ohad on 14/07/2017 for TrueThat.
 */

public class App extends Application {
  private static final String TAG = App.class.getSimpleName();

  @Override public void onCreate() {
    Log.d(TAG, this.getClass().getSimpleName() + " has been created.");
    AppContainer.setDeviceManager(new HardwareDeviceManager(this));
    AppContainer.setPermissionsManager(new DevicePermissionsManager(this));
    AppContainer.setInternalStorageManager(new DeviceInternalStorageManager(this));
    AppContainer.setAuthManager(new BaseAuthManager(AppContainer.getDeviceManager(),
        AppContainer.getInternalStorageManager()));
    AppContainer.setReactionDetectionManager(
        new AffectivaReactionDetectionManager(this, AppContainer.getPermissionsManager()));
    super.onCreate();
    if (!BuildConfig.DEBUG) {
      Fabric.with(this, new Crashlytics());
    }
  }
}
