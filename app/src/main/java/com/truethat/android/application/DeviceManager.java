package com.truethat.android.application;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Proudly created by ohad on 02/07/2017 for TrueThat.
 */

public interface DeviceManager {
  /**
   * @param context to access {@link TelephonyManager}
   *
   * @return a pseudo-unique identifier for the device.
   */
  String getDeviceId(Context context);

  /**
   * @param context to access {@link TelephonyManager}
   *
   * @return device line number. Usually given to it by its SIM card via mobile carriers.
   */
  String getPhoneNumber(Context context);
}
