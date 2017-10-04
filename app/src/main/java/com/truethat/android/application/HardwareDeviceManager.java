package com.truethat.android.application;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Proudly created by ohad on 02/07/2017 for TrueThat.
 */

class HardwareDeviceManager implements DeviceManager {
  private Context mContext;

  HardwareDeviceManager(Context context) {
    mContext = context;
  }

  @SuppressWarnings("deprecation") @SuppressLint("HardwareIds") @Override
  public String getDeviceId() {
    return ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
  }

  @SuppressLint("HardwareIds") @Override public String getPhoneNumber() {
    return ((TelephonyManager) mContext.getSystemService(
        Context.TELEPHONY_SERVICE)).getLine1Number();
  }
}
