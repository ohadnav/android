package com.truethat.android.application.auth;

import android.support.annotation.NonNull;
import android.util.Log;
import com.truethat.android.application.DeviceManager;
import com.truethat.android.application.storage.internal.InternalStorageManager;
import com.truethat.android.common.network.AuthApi;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.model.User;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Proudly created by ohad on 18/07/2017 for TrueThat.
 */

public class BackendAuthManager extends BaseAuthManager {
  /**
   * Auth API interface.
   */
  private AuthApi mAuthApi;
  private Call<User> mAuthCall;

  public BackendAuthManager(DeviceManager deviceManager, InternalStorageManager internalStorage) {
    super(deviceManager, internalStorage);
    mAuthApi = NetworkUtil.createApi(AuthApi.class);
  }

  @Override protected void requestAuth(final AuthListener listener, User user) {
    if (mAuthCall != null) {
      mAuthCall.cancel();
    }
    mAuthCall = mAuthApi.postAuth(user);
    mAuthCall.enqueue(new AuthCallback(listener));
  }

  private class AuthCallback implements Callback<User> {
    private AuthListener mListener;

    AuthCallback(AuthListener listener) {
      mListener = listener;
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
          // Auth had failed
          Log.e(TAG, "Authentication request had failed, inconceivable!", e);
          mCurrentUser = null;
          mListener.onAuthFailed();
        }
      } else {
        Log.e(TAG, "Failed auth request "
            + mAuthCall.request().url()
            + "\n"
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
      // Auth had failed
      Log.e(TAG, "Auth call failed :-(", t);
      mCurrentUser = null;
      mListener.onAuthFailed();
    }
  }
}
