package com.truethat.android.auth;

import android.telephony.TelephonyManager;
import com.truethat.android.model.User;
import com.truethat.android.ui.common.BaseActivity;

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
  void auth(BaseActivity activity);

  /**
   * @return Whether the user in {@link #getUser()} is authorized.
   */
  boolean isAuthOk();
}
