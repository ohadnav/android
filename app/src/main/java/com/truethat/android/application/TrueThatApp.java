package com.truethat.android.application;

import android.app.Application;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.truethat.android.BuildConfig;
import com.truethat.android.di.component.ActivityInjectorComponent;
import com.truethat.android.di.component.AppComponent;
import com.truethat.android.di.component.BaseComponent;
import com.truethat.android.di.component.DaggerActivityInjectorComponent;
import com.truethat.android.di.component.DaggerAppComponent;
import com.truethat.android.di.component.DaggerBaseComponent;
import com.truethat.android.di.module.AppModule;
import com.truethat.android.di.module.NetModule;
import com.truethat.android.ui.activity.BaseActivity;
import javax.inject.Inject;

/**
 * Proudly created by ohad on 14/07/2017 for TrueThat.
 */

public class TrueThatApp extends Application {
  private static final String TAG = TrueThatApp.class.getSimpleName();

  private ActivityInjectorComponent mInjector;

  private AppComponent mAppComponent;

  public void getInjector(BaseActivity activity) {
    mInjector.inject(activity);
  }

  public AppComponent getAppComponent() {
    return mAppComponent;
  }

  @Override public void onCreate() {
    Log.v(TAG, this.getClass().getSimpleName() + " has been created.");
    mAppComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
    BaseComponent baseComponent = DaggerBaseComponent.builder()
        .appComponent(mAppComponent)
        .netModule(new NetModule(BuildConfig.BASE_BACKEND_URL))
        .build();
    mInjector = DaggerActivityInjectorComponent.builder().baseComponent(baseComponent).build();
    super.onCreate();
  }

  @VisibleForTesting public void updateBaseComponent(BaseComponent component) {
    mInjector = DaggerActivityInjectorComponent.builder().baseComponent(component).build();
  }

  @Inject void logInjection() {
    Log.v(TAG, "Injecting " + TrueThatApp.class.getSimpleName());
  }
}
