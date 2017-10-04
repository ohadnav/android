package com.truethat.android.application;

import java.util.Date;

/**
 * Proudly created by ohad on 02/07/2017 for TrueThat.
 */

public class FakeDeviceManager implements DeviceManager {
  private String mPhoneNumber = "+1" + (new Date().getTime() % 1000000000);
  private String mDeviceId = "android-" + new Date().getTime();

  public FakeDeviceManager(String phoneNumber, String deviceId) {
    mPhoneNumber = phoneNumber;
    mDeviceId = deviceId;
  }

  @Override public String getDeviceId() {
    return mDeviceId;
  }

  @Override public String getPhoneNumber() {
    return mPhoneNumber;
  }
}
