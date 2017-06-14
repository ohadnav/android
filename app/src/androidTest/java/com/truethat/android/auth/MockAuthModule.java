package com.truethat.android.auth;

import android.app.Activity;

/**
 * Proudly created by ohad on 29/05/2017 for TrueThat.
 */

public class MockAuthModule implements AuthModule {
  private static final long USER_ID = 1000;
  private static final String USER_NAME = "theRealDonaldTrump";
  private static final String DEVICE_ID = "my-android";
  private static final String PHONE_NUMBER = "+1200300400";
  /**
   * Mocked user for testing.
   */
  private final User USER = new User(USER_ID, USER_NAME, DEVICE_ID, PHONE_NUMBER);

  public User getUser() {
    return USER;
  }

  @Override public void auth(Activity activity) {
  }
}
