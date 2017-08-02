package com.truethat.android.di.module;

import android.app.Application;
import android.content.Context;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/**
 * Proudly created by ohad on 14/07/2017 for TrueThat.
 */

@Module public class AppModule {
  private Application mApplication;

  public AppModule(Application application) {
    mApplication = application;
  }

  @Provides @Singleton Application provideApplication() {
    return mApplication;
  }

  @Provides @Singleton Context provideContext() {
    return mApplication.getApplicationContext();
  }
}
