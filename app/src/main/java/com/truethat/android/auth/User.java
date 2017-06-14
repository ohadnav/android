package com.truethat.android.auth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.truethat.android.application.App;
import com.truethat.android.application.permissions.Permission;
import java.io.Serializable;

/**
 * Proudly created by ohad on 22/05/2017 for TrueThat.
 */

public class User implements Serializable {
  /**
   * User session path, within the app internal storage.
   */
  private static final String USER_PATH = "users/last.user";
  /**
   * Logging tag.
   */
  private static final String TAG = User.class.getSimpleName();
  /**
   * User ID, as stored in our backend.
   */
  private long mId;

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

  @VisibleForTesting public User(long id, String name, String deviceId, String phoneNumber) {
    mId = id;
    mName = name;
    mDeviceId = deviceId;
    mPhoneNumber = phoneNumber;
  }

  /**
   * @param activity application context.
   */
  @SuppressLint("HardwareIds") User(Activity activity) {
    // Trying to retrieve from internal storage.
    if (App.getInternalStorage().exists(activity, USER_PATH)) {
      populateFromInternalStorage(activity);
    }
    App.getPermissionsModule().requestIfNeeded(activity, Permission.PHONE);
    if (App.getPermissionsModule().isPermissionGranted(activity, Permission.PHONE)) {
      TelephonyManager telephonyManager =
          (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
      mDeviceId = telephonyManager.getDeviceId();
      mPhoneNumber = telephonyManager.getLine1Number();
    }
  }

  public long getId() {
    return mId;
  }

  public String getName() {
    return mName;
  }

  private void populateFromInternalStorage(Context context) {
    try {
      User lastUser = App.getInternalStorage().read(context, USER_PATH);
      mId = lastUser.mId;
      mName = lastUser.mName;
      mDeviceId = lastUser.mDeviceId;
      mPhoneNumber = lastUser.mPhoneNumber;
    } catch (Exception e) {
      Log.e(TAG, "Could not read from internal storage. Sad story.. but it's true.", e);
    }
  }
}
