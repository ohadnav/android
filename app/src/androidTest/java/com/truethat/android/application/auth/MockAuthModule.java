package com.truethat.android.application.auth;

import com.truethat.android.model.User;
import com.truethat.android.ui.activity.BaseActivity;

/**
 * Proudly created by ohad on 29/05/2017 for TrueThat.
 */

public class MockAuthModule implements AuthModule {
  private static final long USER_ID = 1000;
  private static final String FIRST_NAME = "Donal";
  private static final String LAST_NAME = "Trump";
  /**
   * Mocked user for testing.
   */
  public static final User USER = new User(USER_ID, FIRST_NAME, LAST_NAME);
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
          mUser = new User(null, null, null);
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

  public void signOut() {
    mUser = null;
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
}
