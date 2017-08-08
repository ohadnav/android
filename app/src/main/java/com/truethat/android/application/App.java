package com.truethat.android.application;

import android.app.Application;
import android.util.Log;
import com.truethat.android.application.auth.BackendAuthManager;
import com.truethat.android.application.permissions.DevicePermissionsManager;
import com.truethat.android.application.storage.internal.DeviceInternalStorageManager;
import com.truethat.android.empathy.AffectivaReactionDetectionManager;

/**
 * Proudly created by ohad on 14/07/2017 for TrueThat.
 */

public class App extends Application {
  private static final String TAG = App.class.getSimpleName();

  @Override public void onCreate() {
    Log.v(TAG, this.getClass().getSimpleName() + " has been created.");
    AppContainer.setDeviceManager(new HardwareDeviceManager(this));
    AppContainer.setPermissionsManager(new DevicePermissionsManager(this));
    AppContainer.setInternalStorageManager(new DeviceInternalStorageManager(this));
    AppContainer.setAuthManager(new BackendAuthManager(AppContainer.getDeviceManager(),
        AppContainer.getInternalStorageManager()));
    AppContainer.setReactionDetectionManager(
        new AffectivaReactionDetectionManager(this, AppContainer.getPermissionsManager()));
    super.onCreate();
  }
}
