package com.truethat.android.identity;

import android.support.annotation.VisibleForTesting;
import java.io.Serializable;

/**
 * Proudly created by ohad on 22/05/2017 for TrueThat.
 */

public class User implements Serializable {
  /**
   * User ID as stored in our backend.
   */
  private long mId;

  /**
   * User name as stored in our backend.
   */
  private String mName;

  @VisibleForTesting public User(long id, String name) {
    mId = id;
    mName = name;
  }

  // TODO(ohad): retrieve logged in user from internal storage.
  User() {
  }

  public long getId() {
    return mId;
  }

  public String getName() {
    return mName;
  }
}
