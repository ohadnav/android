package com.truethat.android.di.module;

import com.truethat.android.application.DeviceManager;
import com.truethat.android.application.auth.AuthManager;
import com.truethat.android.application.auth.DefaultAuthManager;
import com.truethat.android.application.storage.internal.InternalStorageManager;
import com.truethat.android.di.scope.AppScope;
import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

/**
 * Proudly created by ohad on 18/07/2017 for TrueThat.
 */

@Module(includes = {
    NetModule.class, DeviceModule.class, InternalStorageModule.class
}) public class DefaultAuthModule {
  @Provides @AppScope AuthManager provideAuthManager(DeviceManager deviceManager,
      InternalStorageManager internalStorage, Retrofit retrofit) {
    return new DefaultAuthManager(deviceManager, internalStorage, retrofit);
  }
}
