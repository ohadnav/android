package com.truethat.android.application;

import android.app.Application;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.truethat.android.BuildConfig;
import com.truethat.android.di.component.AppComponent;
import com.truethat.android.di.component.AppInjectorComponent;
import com.truethat.android.di.component.DaggerAppComponent;
import com.truethat.android.di.component.DaggerAppInjectorComponent;
import com.truethat.android.di.module.AppModule;
import com.truethat.android.di.module.AuthModule;
import com.truethat.android.di.module.DefaultAuthModule;
import com.truethat.android.di.module.DeviceModule;
import com.truethat.android.di.module.InternalStorageModule;
import com.truethat.android.di.module.NetModule;
import com.truethat.android.di.module.PermissionsModule;
import com.truethat.android.view.activity.BaseActivity;

/**
 * Proudly created by ohad on 14/07/2017 for TrueThat.
 */

public class App extends Application {
  private static final String TAG = App.class.getSimpleName();

  private AppComponent mAppComponent;
  /**
   * Injects dependencies into {@link BaseActivity}.
   */
  private AppInjectorComponent mInjector;

  public AppComponent getAppComponent() {
    return mAppComponent;
  }

  public AppInjectorComponent getInjector() {
    return mInjector;
  }

  @Override public void onCreate() {
    Log.v(TAG, this.getClass().getSimpleName() + " has been created.");
    updateComponents(DaggerAppComponent.builder()
        .appModule(new AppModule(this))
        .netModule(new NetModule(BuildConfig.BASE_BACKEND_URL))
        .permissionsModule(new PermissionsModule())
        .authModule(new AuthModule())
        .defaultAuthModule(new DefaultAuthModule())
        .deviceModule(new DeviceModule())
        .internalStorageModule(new InternalStorageModule())
        .build());
    super.onCreate();
  }

  /**
   * Updates modules container and injection components.
   *
   * @param appComponent to update with.
   */
  @VisibleForTesting public void updateComponents(AppComponent appComponent) {
    mAppComponent = appComponent;
    mInjector = DaggerAppInjectorComponent.builder().appComponent(appComponent).build();
  }
}
