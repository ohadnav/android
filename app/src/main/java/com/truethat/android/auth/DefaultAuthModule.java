package com.truethat.android.auth;

import android.app.Activity;

/**
 * Proudly created by ohad on 29/05/2017 for TrueThat.
 */

public class DefaultAuthModule implements AuthModule {
  /**
   * Currently logged in user.
   */
  private User mUser;

  public DefaultAuthModule() {
  }

  public User getUser(Activity activity) {
    if (mUser == null) {
      // User default constructor is able to retrieve previous sessions.
      mUser = new User(activity);
    }
    return mUser;
  }
}
