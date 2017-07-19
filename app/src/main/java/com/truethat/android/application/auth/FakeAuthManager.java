package com.truethat.android.application.auth;

import com.truethat.android.application.DeviceManager;
import com.truethat.android.application.storage.internal.InternalStorageManager;
import com.truethat.android.model.User;

/**
 * Proudly created by ohad on 29/05/2017 for TrueThat.
 */

public class FakeAuthManager extends BaseAuthManager {

  private static final long USER_ID = 1;
  private static final String FIRST_NAME = "fifty";
  private static final String LAST_NAME = "cent";

  private boolean mAllowAuth = true;

  public FakeAuthManager(DeviceManager deviceManager, InternalStorageManager internalStorage) {
    super(deviceManager, internalStorage);
  }

  public void setAllowAuth(boolean allowAuth) {
    mAllowAuth = allowAuth;
  }

  @Override public boolean isAuthOk() {
    return super.isAuthOk() && mAllowAuth;
  }

  @Override protected void requestAuth(AuthListener listener, User user) {
    if (!user.onBoarded()) {
      user = new User(FIRST_NAME, LAST_NAME, user.getDeviceId(), user.getPhoneNumber());
    }
    user.setId(USER_ID);
    try {
      handleSuccessfulResponse(user);
      listener.onAuthOk();
    } catch (Exception e) {
      listener.onAuthFailed();
    }
  }
}
