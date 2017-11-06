package com.truethat.android.application;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.support.annotation.Nullable;

/**
 * Proudly created by ohad on 02/07/2017 for TrueThat.
 */

class HardwareDeviceManager implements DeviceManager {
  private Context mContext;

  HardwareDeviceManager(Context context) {
    mContext = context;
  }

  @Nullable @SuppressWarnings("deprecation") @SuppressLint("HardwareIds") @Override
  public String getDeviceId() {
    //if (AppContainer.getPermissionsManager().isPermissionGranted(Permission.PHONE)) {
    //  return ((TelephonyManager) mContext.getSystemService(
    //      Context.TELEPHONY_SERVICE)).getDeviceId();
    //}
    //return null;
    return Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
  }

  @Nullable @SuppressLint("HardwareIds") @Override public String getPhoneNumber() {
    //if (AppContainer.getPermissionsManager().isPermissionGranted(Permission.PHONE)) {
    //  return ((TelephonyManager) mContext.getSystemService(
    //      Context.TELEPHONY_SERVICE)).getLine1Number();
    //}
    return null;
  }
}
