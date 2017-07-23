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
import com.truethat.android.di.component.DaggerViewModelInjectorComponent;
import com.truethat.android.di.component.FragmentInjectorComponent;
import com.truethat.android.di.component.ViewModelInjectorComponent;
import com.truethat.android.di.module.AppModule;
import com.truethat.android.di.module.AuthModule;
import com.truethat.android.di.module.DefaultAuthModule;
import com.truethat.android.di.module.DeviceModule;
import com.truethat.android.di.module.InternalStorageModule;
import com.truethat.android.di.module.NetModule;
import com.truethat.android.di.module.PermissionsModule;
import javax.inject.Inject;

/**
 * Proudly created by ohad on 14/07/2017 for TrueThat.
 */

public class App extends Application {
  private static final String TAG = App.class.getSimpleName();

  private AppComponent mAppComponent;
  private ActivityInjectorComponent mActivityInjector;
  private FragmentInjectorComponent mFragmentInjector;
  private ViewModelInjectorComponent mViewModelInjector;

  public AppComponent getAppComponent() {
    return mAppComponent;
  }

  public ViewModelInjectorComponent getViewModelInjector() {
    return mViewModelInjector;
  }

  public ActivityInjectorComponent getActivityInjector() {
    return mActivityInjector;
  }

  public FragmentInjectorComponent getFragmentInjector() {
    return mFragmentInjector;
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
    mAppComponent = appComponent;
    mActivityInjector =
        DaggerActivityInjectorComponent.builder().appComponent(appComponent).build();
    mFragmentInjector =
        DaggerFragmentInjectorComponent.builder().appComponent(appComponent).build();
    mViewModelInjector =
        DaggerViewModelInjectorComponent.builder().appComponent(appComponent).build();
  }

  @Inject void logInjection() {
    Log.v(TAG, "Injecting " + App.class.getSimpleName());
  }
}
