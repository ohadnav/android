package com.truethat.android.di.module.fake;

import com.truethat.android.application.DeviceManager;
import com.truethat.android.application.auth.AuthManager;
import com.truethat.android.application.auth.FakeAuthManager;
import com.truethat.android.application.storage.internal.InternalStorageManager;
import com.truethat.android.di.scope.AppScope;
import dagger.Module;
import dagger.Provides;

/**
 * Proudly created by ohad on 17/07/2017 for TrueThat.
 */

@Module(includes = { FakeDeviceModule.class, FakeInternalStorageModule.class })
public class FakeAuthModule {
  @Provides @AppScope AuthManager provideAuthManager(DeviceManager deviceManager,
      InternalStorageManager internalStorage) {
    return new FakeAuthManager(deviceManager, internalStorage);
  }
}
