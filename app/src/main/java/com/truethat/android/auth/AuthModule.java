package com.truethat.android.auth;

import android.app.Activity;
import android.telephony.TelephonyManager;

/**
 * Proudly created by ohad on 29/05/2017 for TrueThat.
 */

public interface AuthModule {
  String TAG = AuthModule.class.getSimpleName();

  User getUser();

  /**
   * Creates a User instance, and authenticate it against our backend.
   *
   * @param activity for which to auth. We need its context to access internal storage and {@link
   * TelephonyManager}.
   */
  void auth(Activity activity);
}