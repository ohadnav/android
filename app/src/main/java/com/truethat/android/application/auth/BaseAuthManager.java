package com.truethat.android.application.auth;

import android.support.annotation.NonNull;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.truethat.android.BuildConfig;
import com.truethat.android.application.DeviceManager;
import com.truethat.android.application.LoggingKey;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.application.storage.internal.InternalStorageManager;
import com.truethat.android.common.network.AuthApi;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.model.User;
import com.truethat.android.view.activity.BaseActivity;
import java.io.IOException;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Proudly created by ohad on 29/05/2017 for TrueThat.
 */

public class BaseAuthManager implements AuthManager {
  Call<User> mAuthCall;
  /**
   * Currently logged in user.
   */
  private User mCurrentUser;
  /**
   * Auth API interface.
   */
  private AuthApi mAuthApi;
  private DeviceManager mDeviceManager;
  private InternalStorageManager mInternalStorage;

  public BaseAuthManager(DeviceManager deviceManager, InternalStorageManager internalStorage) {
    mDeviceManager = deviceManager;
    mInternalStorage = internalStorage;
    mAuthApi = NetworkUtil.createApi(AuthApi.class);
  }

  public User getCurrentUser() {
    return mCurrentUser;
  }

  /**
   * Authenticate user synchronously, to ensure a {@link User} can be retrieved from {@link
   * #getCurrentUser()}
   * <p>
   * If {@link Permission#PHONE} is not granted then the authentication will terminate and will be
   * run again on {@link BaseActivity#onResume()}
   *
   * @param listener for which to auth.
   */
  @Override public void auth(final AuthListener listener) {
    // TODO(ohad): handle device id/ phone-number replacement.
    try {
      if (mCurrentUser == null && mInternalStorage.read(AuthManager.LAST_USER_PATH) != null) {
        // Retrieves last session, and auth against backend
        requestAuth(listener, (User) mInternalStorage.read(AuthManager.LAST_USER_PATH));
      } else {
        if (isAuthOk()) {
          // Already authenticated
          listener.onAuthOk();
        } else {
          // Signed out
          mCurrentUser = null;
          listener.onAuthFailed();
        }
      }
    } catch (Exception e) {
      Log.e(TAG, "Could not auth. Sad story.. but it's true.", e);
      mCurrentUser = null;
      listener.onAuthFailed();
    }
  }

  @Override public void signIn(AuthListener listener) {
    if (isAuthOk()) {
      // Already authenticated
      Log.v(TAG, "Trying to sign in when already logged in. Bad logic flow?");
      listener.onAuthOk();
    } else {
      User userToSignIn = null;
      try {
        userToSignIn = mInternalStorage.read(AuthManager.LAST_USER_PATH);
      } catch (IOException | ClassNotFoundException exception) {
        Log.w(TAG, "Failed to read last user session from storage. "
            + "Trying to sign in based on device id.");
      }
      if (userToSignIn == null) {
        userToSignIn = new User(mDeviceManager.getDeviceId());
      } else if (userToSignIn.getDeviceId() == null) {
        userToSignIn.setDeviceId(mDeviceManager.getDeviceId());
      }
      requestAuth(listener, userToSignIn);
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
    Log.d(TAG,
        "Signing " + (mCurrentUser != null ? mCurrentUser.getDisplayName() : "") + " out...");
    // Deletes user session.
    mCurrentUser = null;
    // Deletes user session from storage.
    try {
      mInternalStorage.delete(AuthManager.LAST_USER_PATH);
    } catch (IOException e) {
      e.printStackTrace();
      Log.e(TAG, "Failed to delete user session from storage.", e);
    }
    listener.onAuthFailed();
  }

  @Override public void cancelRequest() {
    if (mAuthCall != null && !mAuthCall.isCanceled()) {
      mAuthCall.cancel();
    }
  }

  /**
   * Authenticates user against our lovely backend.
   *
   * @param listener to use for auth callbacks
   */
  void requestAuth(final AuthListener listener, User user) {
    cancelRequest();
    mAuthCall = mAuthApi.postAuth(user);
    mAuthCall.enqueue(new AuthCallback(listener, user));
    if (!BuildConfig.DEBUG) {
      Crashlytics.setString(LoggingKey.AUTH_USER.name(), user.toString());
    }
  }

  void handleSuccessfulResponse(User respondedUser) throws IOException {
    mCurrentUser = respondedUser;
    mInternalStorage.write(AuthManager.LAST_USER_PATH, mCurrentUser);
    Log.v(TAG, mCurrentUser.getDisplayName() + " is authenticated.");
    if (!BuildConfig.DEBUG) {
      Crashlytics.setUserIdentifier(Long.toString(mCurrentUser.getId()));
      Crashlytics.setUserName(mCurrentUser.getDisplayName());
    }
  }

  private class AuthCallback implements Callback<User> {
    private AuthListener mListener;
    private User mUser;

    AuthCallback(AuthListener listener, User user) {
      mListener = listener;
      mUser = user;
    }

    @Override public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
      if (response.isSuccessful()) {
        try {
          User respondedUser = response.body();
          if (respondedUser == null) {
            throw new AssertionError("Responded user is a ghost... scary.");
          }
          handleSuccessfulResponse(respondedUser);
          mListener.onAuthOk();
        } catch (IOException | AssertionError e) {
          Crashlytics.logException(e);
          e.printStackTrace();
          // Auth had failed
          Log.e(TAG, "Authentication request had failed, inconceivable!", e);
          mCurrentUser = null;
          mListener.onAuthFailed();
        }
      } else {
        Buffer buffer = new Buffer();
        try {
          //noinspection ConstantConditions
          call.request().body().writeTo(buffer);
        } catch (Exception e) {
          e.printStackTrace();
        }
        String requestBody = buffer.readUtf8();
        Log.e(TAG, "Failed auth request to "
            + call.request().url() + "\nBody: " + requestBody
            + "\nUser: "
            + mUser
            + "\nResponse: "
            + response.code()
            + " "
            + response.message()
            + "\n"
            + response.headers());
        mCurrentUser = null;
        mListener.onAuthFailed();
      }
    }

    @Override public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
      if (!BuildConfig.DEBUG) {
        Crashlytics.logException(t);
      }
      t.printStackTrace();
      // Auth had failed
      Log.e(TAG, "Auth call failed: " + t.getMessage(), t);
      mCurrentUser = null;
      mListener.onAuthFailed();
    }
  }
}
