package com.truethat.android.di.module;

import android.content.Context;
import com.truethat.android.application.DefaultDeviceManager;
import com.truethat.android.application.DeviceManager;
import com.truethat.android.di.scope.AppScope;
import dagger.Module;
import dagger.Provides;

/**
 * Proudly created by ohad on 17/07/2017 for TrueThat.
 */

@Module(includes = AppModule.class) public class DeviceModule {
  @Provides @AppScope DeviceManager provideDeviceManager(Context context) {
    return new DefaultDeviceManager(context);
  }
}
