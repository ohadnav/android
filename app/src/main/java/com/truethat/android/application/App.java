package com.truethat.android.application;

import android.app.Application;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.truethat.android.BuildConfig;
import com.truethat.android.di.component.ActivityInjectorComponent;
import com.truethat.android.di.component.AppComponent;
import com.truethat.android.di.component.DaggerActivityInjectorComponent;
import com.truethat.android.di.component.DaggerAppComponent;
import com.truethat.android.di.component.DaggerFragmentInjectorComponent;
import com.truethat.android.di.component.FragmentInjectorComponent;
import com.truethat.android.di.module.AppModule;
import com.truethat.android.di.module.AuthModule;
import com.truethat.android.di.module.DefaultAuthModule;
import com.truethat.android.di.module.DeviceModule;
import com.truethat.android.di.module.InternalStorageModule;
import com.truethat.android.di.module.NetModule;
import com.truethat.android.di.module.PermissionsModule;
import com.truethat.android.ui.activity.BaseActivity;
import com.truethat.android.ui.common.BaseFragment;
import com.truethat.android.ui.common.media.ReactableFragment;
import javax.inject.Inject;

/**
 * Proudly created by ohad on 14/07/2017 for TrueThat.
 */

public class App extends Application {
  private static final String TAG = App.class.getSimpleName();

  private ActivityInjectorComponent mActivityInjectorComponent;
  private FragmentInjectorComponent mFragmentInjectorComponent;

  public void inject(BaseActivity activity) {
    mActivityInjectorComponent.inject(activity);
  }

  public void inject(BaseFragment fragment) {
    mFragmentInjectorComponent.inject(fragment);
  }

  public void inject(ReactableFragment reactableFragment) {
    mFragmentInjectorComponent.inject(reactableFragment);
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

  @VisibleForTesting public void updateComponents(AppComponent appComponent) {
    mActivityInjectorComponent =
        DaggerActivityInjectorComponent.builder().appComponent(appComponent).build();
    mFragmentInjectorComponent =
        DaggerFragmentInjectorComponent.builder().appComponent(appComponent).build();
  }

  @Inject void logInjection() {
    Log.v(TAG, "Injecting " + App.class.getSimpleName());
  }
}
