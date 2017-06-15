package com.truethat.android.auth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.telephony.TelephonyManager;
import com.truethat.android.application.App;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

/**
 * Proudly created by ohad on 22/05/2017 for TrueThat.
 *
 * @backend https://github.com/true-that/backend/blob/master/src/main/java/com/truethat/backend/model/User.java
 */

public class User implements Serializable {
  /**
   * User session path, within the app internal storage.
   */
  @VisibleForTesting static final String LAST_USER_PATH = "users/last.user";
  /**
   * Logging tag.
   */
  private static final String TAG = User.class.getSimpleName();
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
  private String mDeviceId;
  /**
   * Current phone number.
   */
  private String mPhoneNumber;

  @VisibleForTesting
  public User(long id, String firstName, String lastName, String deviceId, String phoneNumber) {
    mId = id;
    mFirstName = firstName;
    mLastName = lastName;
    mDeviceId = deviceId;
    mPhoneNumber = phoneNumber;
  }

  /**
   * @param context to access internal storage and {@link TelephonyManager}.
   */
  @SuppressLint("HardwareIds") User(Context context) throws IOException, ClassNotFoundException {
    // Trying to retrieve previous session from internal storage.
    if (App.getInternalStorage().exists(context, LAST_USER_PATH)) {
      populateFromInternalStorage(context);
    }
    TelephonyManager telephonyManager =
        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    mDeviceId = telephonyManager.getDeviceId();
    mPhoneNumber = telephonyManager.getLine1Number();
  }

  public long getId() {
    if (mId == null) {
      throw new IllegalStateException("User ID had not been initialized.");
    }
    return mId;
  }

  public String getDisplayName() {
    if (mFirstName == null || mLastName == null) {
      throw new IllegalStateException("User had not been initialized.");
    }
    return mFirstName + " " + mLastName;
  }

  /**

   * Updates this user instance using another one. Only updates {@code mId} and {@code mName}.
   *
   * @param user to copy fields from.
   * @param context to access internal storage.
   */
  void update(User user, Context context) throws IOException {
    boolean modified = false;
    if (user.mFirstName != null && !Objects.equals(mFirstName, user.mFirstName)) {
      mFirstName = user.mFirstName;
      modified = true;
    }
    if (user.mLastName != null && !Objects.equals(mLastName, user.mLastName)) {
      mLastName = user.mLastName;
      modified = true;
    }
    if (user.mId != null && !Objects.equals(mId, user.mId)) {
      mId = user.mId;
      modified = true;
    }
    if (modified) {
      save(context);
    }
  }

  /**
   * Saves this user to internal storage, for faster application bootstrap in future sessions.
   *
   * @param context to access internal storage.
   */
  void save(Context context) throws IOException {
    App.getInternalStorage().write(context, LAST_USER_PATH, this);
  }

  boolean hasId() {
    return mId != null;
  }

  private void populateFromInternalStorage(Context context)
      throws IOException, ClassNotFoundException {
    User lastUser = App.getInternalStorage().read(context, LAST_USER_PATH);
    mId = lastUser.mId;
    mFirstName = lastUser.mFirstName;
    mLastName = lastUser.mLastName;
    mDeviceId = lastUser.mDeviceId;
    mPhoneNumber = lastUser.mPhoneNumber;
  }
}
