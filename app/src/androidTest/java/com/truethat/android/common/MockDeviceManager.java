package com.truethat.android.common;

import android.content.Context;
import com.truethat.android.application.DeviceManager;
import java.util.Date;

/**
 * Proudly created by ohad on 02/07/2017 for TrueThat.
 */

class MockDeviceManager implements DeviceManager {
  private String mDeviceId = "android-" + new Date().getTime();
  private String mPhoneNumber = "+" + new Date().getTime();

  MockDeviceManager() {
  }

  MockDeviceManager(String deviceId, String phoneNumber) {
    mDeviceId = deviceId;
    mPhoneNumber = phoneNumber;
  }

  @Override public String getDeviceId(Context context) {
    return mDeviceId;
  }

  @Override public String getPhoneNumber(Context context) {
    return mPhoneNumber;
  }
}
