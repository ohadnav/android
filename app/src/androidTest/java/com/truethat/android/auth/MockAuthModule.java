package com.truethat.android.auth;

import com.truethat.android.model.User;
import com.truethat.android.ui.common.BaseActivity;

/**
 * Proudly created by ohad on 29/05/2017 for TrueThat.
 */

public class MockAuthModule implements AuthModule {
  private static final long USER_ID = 1000;
  private static final String FIRST_NAME = "Donal";
  private static final String LAST_NAME = "Trump";
  private static final String DEVICE_ID = "my-android";
  private static final String PHONE_NUMBER = "+1200300400";
  /**
   * Mocked user for testing.
   */
  public static final User USER = new User(USER_ID, FIRST_NAME, LAST_NAME, DEVICE_ID, PHONE_NUMBER);
  /**
   * Whether auth requests should be authorized.
   */
  private boolean mAllowAuth = true;
  /**
   * Whether the user had already been through on-boarding, i.e. had he or she created an account.
   */
  private boolean mOnBoarded = true;
  /**
   * Currently logged in user.
   */
  private User mUser;

  public User getUser() {
    return mUser;
  }

  @Override public void auth(final BaseActivity activity) {
    if (!mOnBoarded) {
      activity.runOnUiThread(new Runnable() {
        @Override public void run() {
          mUser = new User(null, null, null, DEVICE_ID, PHONE_NUMBER);
          activity.onBoarding();
          mOnBoarded = true;
        }
      });
    } else if (mAllowAuth) {
      activity.runOnUiThread(new Runnable() {
        @Override public void run() {
          if (mUser == null) {
            mUser = USER;
          } else {
            mUser.setId(USER_ID);
          }
          activity.onAuthOk();
        }
      });
    } else {
      activity.runOnUiThread(new Runnable() {
        @Override public void run() {
          signOut();
          activity.onAuthFailed();
        }
      });
    }
  }

  @Override public boolean isAuthOk() {
    return mUser != null && mUser.isAuthOk();
  }

  public void setAllowAuth(boolean allowAuth) {
    if (!allowAuth) {
      signOut();
    }
    mAllowAuth = allowAuth;
  }

  public void setOnBoarded(boolean onBoarded) {
    if (!onBoarded) {
      signOut();
    }
    mOnBoarded = onBoarded;
  }

  /**
   * Deletes current user session.
   */
  private void signOut() {
    mUser = null;
  }
}
