package com.truethat.android.application;

import java.util.Date;

/**
 * Proudly created by ohad on 02/07/2017 for TrueThat.
 */

public class FakeDeviceManager implements DeviceManager {
  private String mDeviceId = "android-" + new Date().getTime();
  private String mPhoneNumber = "+" + new Date().getTime();

  public FakeDeviceManager(String deviceId, String phoneNumber) {
    mDeviceId = deviceId;
    mPhoneNumber = phoneNumber;
  }

  @Override public String getDeviceId() {
    return mDeviceId;
  }

  @Override public String getPhoneNumber() {
    return mPhoneNumber;
  }
}
