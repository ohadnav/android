package com.truethat.android.application;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Proudly created by ohad on 02/07/2017 for TrueThat.
 */

class DefaultDeviceManager implements DeviceManager {
  @SuppressLint("HardwareIds") @Override public String getDeviceId(Context context) {
    return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
  }

  @SuppressLint("HardwareIds") @Override public String getPhoneNumber(Context context) {
    return ((TelephonyManager) context.getSystemService(
        Context.TELEPHONY_SERVICE)).getLine1Number();
  }
}
