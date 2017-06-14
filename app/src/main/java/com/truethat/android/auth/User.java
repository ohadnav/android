package com.truethat.android.auth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.truethat.android.application.App;
import java.io.IOException;
import java.io.Serializable;

/**
 * Proudly created by ohad on 22/05/2017 for TrueThat.
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
   * Displayed name.
   */
  private String mName;
  /**
   * Android ID.
   */
  private String mDeviceId;
  /**
   * Current phone number.
   */
  private String mPhoneNumber;
  /**
   * Context to access internal storage and {@link TelephonyManager}.
   */
  private transient Context mContext;

  @VisibleForTesting public User(long id, String name, String deviceId, String phoneNumber) {
    mId = id;
    mName = name;
    mDeviceId = deviceId;
    mPhoneNumber = phoneNumber;
  }

  /**
   * @param context application context.
   */
  @SuppressLint("HardwareIds") User(Context context) {
    mContext = context;
    // Trying to retrieve previous session from internal storage.
    if (App.getInternalStorage().exists(mContext, LAST_USER_PATH)) {
      populateFromInternalStorage();
    }
    TelephonyManager telephonyManager =
        (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
    mDeviceId = telephonyManager.getDeviceId();
    mPhoneNumber = telephonyManager.getLine1Number();
  }

  public long getId() {
    if (mId == null) {
      throw new IllegalStateException("User ID had not been initialized.");
    }
    return mId;
  }

  public void setId(Long id) {
    mId = id;
  }

  public String getName() {
    return mName;
  }

  boolean hasId() {
    return mId != null;
  }

  /**
   * Saves this user to internal storage, for faster application bootstrap in future sessions.
   */
  void save() throws IOException {
    App.getInternalStorage().write(mContext, LAST_USER_PATH, this);
  }

  private void populateFromInternalStorage() {
    try {
      User lastUser = App.getInternalStorage().read(mContext, LAST_USER_PATH);
      mId = lastUser.mId;
      mName = lastUser.mName;
      mDeviceId = lastUser.mDeviceId;
      mPhoneNumber = lastUser.mPhoneNumber;
    } catch (Exception e) {
      Log.e(TAG, "Could not read from internal storage. Sad story.. but it's true.", e);
    }
  }
}
