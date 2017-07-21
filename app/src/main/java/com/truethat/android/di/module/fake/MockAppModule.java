package com.truethat.android.di.module.fake;

import android.content.Context;
import com.truethat.android.di.scope.AppScope;
import dagger.Module;
import dagger.Provides;

/**
 * Proudly created by ohad on 20/07/2017 for TrueThat.
 */

@Module public class MockAppModule {
  private Context mContext;

  public MockAppModule(Context mockContext) {
    mContext = mockContext;
  }

  @Provides @AppScope Context provideContext() {
    return mContext;
  }
}
