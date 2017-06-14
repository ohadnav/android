package com.truethat.android.auth;

import android.app.Activity;

/**
 * Proudly created by ohad on 29/05/2017 for TrueThat.
 */

public interface AuthModule {
  User getUser(Activity activity);
}
