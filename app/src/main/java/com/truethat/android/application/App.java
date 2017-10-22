package com.truethat.android.application;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.truethat.android.BuildConfig;
import com.truethat.android.application.auth.BaseAuthManager;
import com.truethat.android.application.permissions.DevicePermissionsManager;
import com.truethat.android.application.storage.internal.DeviceInternalStorageManager;
import com.truethat.android.empathy.AffectivaReactionDetectionManager;
import com.truethat.android.view.activity.BaseActivity;
import io.fabric.sdk.android.Fabric;

/**
 * Proudly created by ohad on 14/07/2017 for TrueThat.
 */

public class App extends Application implements Application.ActivityLifecycleCallbacks {
  private static final String TAG = App.class.getSimpleName();
  private Activity mResumedActivity;

  @Override public void onCreate() {
    for (int i = 0; i < 10; i++) {
      Log.d(TAG,
          "************************ !!!!! LAUNCHED !!!!! ************************************************ !!!!! LAUNCHED !!!!! ************************");
    }
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
    // Register to be notified of activity state changes
    registerActivityLifecycleCallbacks(this);
  }

  public boolean isResumed(BaseActivity activity) {
    return mResumedActivity == activity;
  }

  @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

  }

  @Override public void onActivityStarted(Activity activity) {

  }

  @Override public void onActivityResumed(Activity activity) {
    mResumedActivity = activity;
  }

  @Override public void onActivityPaused(Activity activity) {

  }

  @Override public void onActivityStopped(Activity activity) {

  }

  @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

  }

  @Override public void onActivityDestroyed(Activity activity) {

  }
}
