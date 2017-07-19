package com.truethat.android.application.auth;

import android.util.Log;
import com.truethat.android.application.DeviceManager;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.application.storage.internal.InternalStorageManager;
import com.truethat.android.model.User;
import com.truethat.android.view.activity.BaseActivity;
import java.io.IOException;

/**
 * Proudly created by ohad on 29/05/2017 for TrueThat.
 */

abstract class BaseAuthManager implements AuthManager {
  /**
   * Currently logged in user.
   */
  private User mCurrentUser;

  private DeviceManager mDeviceManager;
  private InternalStorageManager mInternalStorage;

  BaseAuthManager(DeviceManager deviceManager, InternalStorageManager internalStorage) {
    mDeviceManager = deviceManager;
    mInternalStorage = internalStorage;
  }

  public User currentUser() {
    return mCurrentUser;
  }

  /**
   * Authenticate user synchronously, to ensure a {@link User} can be retrieved from {@link
   * #currentUser()}
   * <p>
   * If {@link Permission#PHONE} is not granted then the authentication will terminate and will be
   * run again on {@link BaseActivity#onResume()}
   *
   * @param listener for which to auth.
   */
  @Override public void auth(final AuthListener listener) {
    // TODO(ohad): handle device id/ phone-number replacement.
    try {
      if (mCurrentUser == null) {
        // Tries to retrieve last session
        mCurrentUser = mInternalStorage.read(AuthManager.LAST_USER_PATH);
      }
      // TODO(ohad): Validate current user against our backend.
      if (isAuthOk()) {
        // Already authenticated
        listener.onAuthOk();
      } else {
        // Signed out
        listener.onAuthFailed();
      }
    } catch (Exception e) {
      Log.e(TAG, "Could not auth. Sad story.. but it's true.", e);
      listener.onAuthFailed();
    }
  }

  @Override public void signIn(AuthListener listener) {
    if (isAuthOk()) {
      // Already authenticated
      Log.v(TAG, "Trying to sign in when already logged in. Bad logic flow?");
      listener.onAuthOk();
    } else {
      User userByDevice = new User(mDeviceManager.getDeviceId(), mDeviceManager.getPhoneNumber());
      requestAuth(listener, userByDevice);
    }
  }

  @Override public void signUp(final AuthListener listener, User newUser) {
    if (isAuthOk()) {
      // Already authenticated
      Log.w(TAG, "Trying to sign up when already logged in. Bad logic flow?");
      listener.onAuthOk();
    } else {
      requestAuth(listener, newUser);
    }
  }

  @Override public boolean isAuthOk() {
    return mCurrentUser != null && mCurrentUser.isAuthOk();
  }

  @Override public void signOut(AuthListener listener) {
    Log.v(TAG,
        "Signing " + (mCurrentUser != null ? mCurrentUser.getDisplayName() : "") + " out...");
    // Deletes user session.
    mCurrentUser = null;
    // Deletes user session from storage.
    try {
      mInternalStorage.delete(AuthManager.LAST_USER_PATH);
    } catch (IOException e) {
      Log.e(TAG, "Failed to delete user session from storage.", e);
    }
    listener.onAuthFailed();
  }

  /**
   * Authenticates user against our lovely backend.
   *
   * @param listener to use for auth callbacks
   */
  protected abstract void requestAuth(final AuthListener listener, User user);

  void handleSuccessfulResponse(User respondedUser) throws IOException {
    mCurrentUser = respondedUser;
    mInternalStorage.write(AuthManager.LAST_USER_PATH, mCurrentUser);
    Log.v(TAG, mCurrentUser.getDisplayName() + " is authenticated.");
  }
}
