package com.truethat.android.auth;

import com.truethat.android.common.BaseActivity;

/**
 * Proudly created by ohad on 29/05/2017 for TrueThat.
 */

public class MockAuthModule implements AuthModule {
  private static final long USER_ID = 1000;
  private static final String FIRST_NAME = "Fo";
  private static final String LAST_NAME = "Real";
  private static final String DEVICE_ID = "my-android";
  private static final String PHONE_NUMBER = "+1200300400";
  /**
   * Mocked user for testing.
   */
  private final User USER = new User(USER_ID, FIRST_NAME, LAST_NAME, DEVICE_ID, PHONE_NUMBER);

  /**
   * Whether auth reuqets should be allowed.
   */
  private boolean mAllowAuth = true;

  public User getUser() {
    return USER;
  }

  void setAllowAuth(boolean allowAuth) {
    mAllowAuth = allowAuth;
  }

  @Override public void auth(BaseActivity activity) {
    if (!mAllowAuth) activity.onAuthFailed();
  }
}
