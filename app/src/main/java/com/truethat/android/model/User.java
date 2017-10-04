package com.truethat.android.model;

import android.support.annotation.VisibleForTesting;
import com.truethat.android.application.DeviceManager;
import java.io.Serializable;

import static com.truethat.android.common.util.StringUtil.toTitleCase;

/**
 * Proudly created by ohad on 22/05/2017 for TrueThat.
 *
 * @backend <a>https://github.com/true-that/backend/blob/master/src/main/java/com/truethat/backend/model/User.java</a>
 */

public class User extends BaseModel implements Serializable {
  private static final long serialVersionUID = -985041994101025724L;
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
  private String mDeviceId;
  /**
   * Phone number of the user.
   */
  private String mPhoneNumber;

  public User(DeviceManager deviceManager) {
    updateUser(deviceManager);
  }

  @VisibleForTesting public User(Long id, String firstName, String lastName, String deviceId) {
    super(id);
    mFirstName = firstName;
    mLastName = lastName;
    mDeviceId = deviceId;
  }

  public User(String firstName, String lastName, String deviceId, String phoneNumber) {
    mFirstName = firstName;
    mLastName = lastName;
    mDeviceId = deviceId;
    mPhoneNumber = phoneNumber;
  }

  @SuppressWarnings("SameParameterValue") @VisibleForTesting
  public User(Long id, String firstName, String lastName) {
    super(id);
    mFirstName = firstName;
    mLastName = lastName;
  }

  public String getDeviceId() {
    return mDeviceId;
  }

  public void setDeviceId(String deviceId) {
    mDeviceId = deviceId;
  }

  public String getPhoneNumber() {
    return mPhoneNumber;
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

  public void updateUser(DeviceManager deviceManager) {
    if (deviceManager.getDeviceId() != null) {
      mDeviceId = deviceManager.getDeviceId();
    }
    if (deviceManager.getPhoneNumber() != null) {
      mPhoneNumber = deviceManager.getPhoneNumber();
    }
  }

  /**
   * @return whether the user had been through on boarding.
   */
  public boolean onBoarded() {
    return mFirstName != null && mLastName != null;
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (mFirstName != null ? mFirstName.hashCode() : 0);
    result = 31 * result + (mLastName != null ? mLastName.hashCode() : 0);
    result = 31 * result + (mDeviceId != null ? mDeviceId.hashCode() : 0);
    return result;
  }

  @SuppressWarnings("SimplifiableIfStatement") @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User)) return false;
    if (!super.equals(o)) return false;

    User user = (User) o;

    if (mFirstName != null ? !mFirstName.equals(user.mFirstName) : user.mFirstName != null) {
      return false;
    }
    if (mLastName != null ? !mLastName.equals(user.mLastName) : user.mLastName != null) {
      return false;
    }
    return mDeviceId != null ? mDeviceId.equals(user.mDeviceId) : user.mDeviceId == null;
  }
}
