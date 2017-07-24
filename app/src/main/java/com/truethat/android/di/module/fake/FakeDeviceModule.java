package com.truethat.android.di.module.fake;

import com.truethat.android.application.DeviceManager;
import com.truethat.android.application.FakeDeviceManager;
import com.truethat.android.di.scope.AppScope;
import dagger.Module;
import dagger.Provides;

/**
 * Proudly created by ohad on 18/07/2017 for TrueThat.
 */

@Module public class FakeDeviceModule {
  private static final String DEVICE_ID = "android1";
  private static final String PHONE_NUMBER = "911";

  private String mDeviceId;
  private String mPhoneNumber;

  public FakeDeviceModule() {
    mDeviceId = DEVICE_ID;
    mPhoneNumber = PHONE_NUMBER;
  }

  @Provides @AppScope DeviceManager provideDeviceManager() {
    return new FakeDeviceManager(mDeviceId, mPhoneNumber);
  }
}
