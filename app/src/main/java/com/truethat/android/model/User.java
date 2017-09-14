package com.truethat.android.model;

import android.support.annotation.VisibleForTesting;
import com.truethat.android.common.network.NetworkUtil;
import java.io.Serializable;

import static com.truethat.android.common.util.StringUtil.toTitleCase;

/**
 * Proudly created by ohad on 22/05/2017 for TrueThat.
 *
 * @backend <a>https://github.com/true-that/backend/blob/master/src/main/java/com/truethat/backend/model/User.java</a>
 */

public class User implements Serializable {
  private static final long serialVersionUID = 2497412482569724543L;
  /**
   * User ID, as stored in our backend.
   */
  private Long mId;
  /**
   * First name.
   */
  private String mFirstName;
  /**
   * Last name.
   */
  private String mLastName;
  /**
   * Android ID.
   */
  @SuppressWarnings("unused") private String mDeviceId;

  public User(String deviceId) {
    mDeviceId = deviceId;
  }

  public User(String firstName, String lastName, String deviceId) {
    mFirstName = firstName;
    mLastName = lastName;
    mDeviceId = deviceId;
  }

  @VisibleForTesting public User(Long id, String firstName, String lastName, String deviceId) {
    mId = id;
    mFirstName = firstName;
    mLastName = lastName;
    mDeviceId = deviceId;
  }

  @SuppressWarnings("SameParameterValue") @VisibleForTesting
  public User(Long id, String firstName, String lastName) {
    mId = id;
    mFirstName = firstName;
    mLastName = lastName;
  }

  public Long getId() {
    return mId;
  }

  public void setId(Long id) {
    mId = id;
  }

  public String getDisplayName() {
    if (mFirstName == null || mLastName == null) {
      throw new IllegalStateException("User had not been initialized: " + this);
    }
    return toTitleCase(mFirstName + " " + mLastName);
  }

  /**
   * Whether this User is seemingly have been through authentication.
   *
   * @return whether ID, first and last names are all non-null.
   */
  public boolean isAuthOk() {
    return mId != null && onBoarded();
  }

  public String getDeviceId() {
    return mDeviceId;
  }

  public void setDeviceId(String deviceId) {
    mDeviceId = deviceId;
  }

  /**
   * @return whether the user had been through on boarding.
   */
  public boolean onBoarded() {
    return mFirstName != null && mLastName != null;
  }

  @Override public int hashCode() {
    int result = mId != null ? mId.hashCode() : 0;
    result = 31 * result + (mFirstName != null ? mFirstName.hashCode() : 0);
    result = 31 * result + (mLastName != null ? mLastName.hashCode() : 0);
    result = 31 * result + (mDeviceId != null ? mDeviceId.hashCode() : 0);
    return result;
  }

  @SuppressWarnings("SimplifiableIfStatement") @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User)) return false;

    User user = (User) o;

    if (mId != null ? !mId.equals(user.mId) : user.mId != null) return false;
    if (mFirstName != null ? !mFirstName.equals(user.mFirstName) : user.mFirstName != null) {
      return false;
    }
    if (mLastName != null ? !mLastName.equals(user.mLastName) : user.mLastName != null) {
      return false;
    }
    return mDeviceId != null ? mDeviceId.equals(user.mDeviceId) : user.mDeviceId == null;
  }

  @Override public String toString() {
    return NetworkUtil.GSON.toJson(this);
  }
}
