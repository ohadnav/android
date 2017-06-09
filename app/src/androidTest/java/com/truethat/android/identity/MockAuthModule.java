package com.truethat.android.identity;

/**
 * Proudly created by ohad on 29/05/2017 for TrueThat.
 */

public class MockAuthModule implements AuthModule {
  private static final long USER_ID = 1010;
  private static final String USER_NAME = "theRealDonaldTrump";
  private final User USER = new User(USER_ID, USER_NAME);

  @Override public User getCurrentUser() {
    return USER;
  }
}
