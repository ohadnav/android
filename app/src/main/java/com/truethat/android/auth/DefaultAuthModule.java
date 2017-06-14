package com.truethat.android.auth;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import com.truethat.android.application.App;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.common.BaseActivity;
import com.truethat.android.common.network.NetworkUtil;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Proudly created by ohad on 29/05/2017 for TrueThat.
 */

public class DefaultAuthModule implements AuthModule {
  /**
   * Currently logged in user.
   */
  private User mUser;
  /**
   * Auth API interface.
   */
  private AuthAPI mAuthAPI = NetworkUtil.createAPI(AuthAPI.class);
  private Call<User> mAuthCall;

  public DefaultAuthModule() {
  }

  public User getUser() {
    if (!mUser.hasId()) {
      Log.v(TAG, "No user ID yet. Performing synchronous authentication.");
      authSync();
    }
    return mUser;
  }

  /**
   * If {@link Permission#PHONE} is not granted then the authentication will terminate and will be
   * run again on {@link BaseActivity#onResume()}
   *
   * @param activity for which to auth.
   */
  @Override public void auth(final Activity activity) {
    if (mUser == null) {
      App.getPermissionsModule().requestIfNeeded(activity, Permission.PHONE);
      if (!App.getPermissionsModule().isPermissionGranted(activity, Permission.PHONE)) {
        return;
      }
      mUser = new User(activity);
    }
    // If the user has an ID, don't post an auth request.
    if (!mUser.hasId()) {
      // Get user ID from server, if needed.
      mAuthCall = mAuthAPI.postAuth(mUser);
      mAuthCall.enqueue(new Callback<User>() {
        @Override
        public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
          handleResponse(call, response);
        }

        @Override public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
          Log.e(TAG, "Asynchronous authentication to " + call.request().url() + " had failed.", t);
        }
      });
    }
  }

  /**
   * Authenticate user synchronously. Used to ensure a {@link User} can be retrieved from {@link
   * #getUser()}
   */
  private void authSync() {
    if (mUser == null) {
      throw new IllegalStateException(
          "Must call #auth() first before #authSync, to initialize mUser.");
    }
    if (mAuthCall != null) {
      // Cancel existing request.
      mAuthCall.cancel();
    }
    mAuthCall = mAuthAPI.postAuth(mUser);
    try {
      handleResponse(mAuthCall, mAuthCall.execute());
    } catch (IOException e) {
      Log.e(TAG, "Synchronous authentication had failed.", e);
    }
  }

  /**
   * Handles Auth API HTTP response
   *
   * @param call HTTP request
   * @param response HTTP request. Note that in case of unauthorized users, a null response body is
   * returned.
   */
  private void handleResponse(Call<User> call, Response<User> response) {
    if (response.isSuccessful() && response.body() != null) {
      User respondedUser = response.body();
      if (respondedUser == null) {
        throw new AssertionError("Responded user is a ghost... scary.");
      }
      mUser.setId(respondedUser.getId());
      try {
        mUser.save();
      } catch (IOException e) {
        Log.e(TAG, "Failed to save user to internal storage.", e);
      }
    } else {
      // TODO(ohad): handle unauthorized users.
      Log.e(TAG, "Failed to post event to "
          + call.request().url()
          + "\n"
          + response.code()
          + " "
          + response.message()
          + "\n"
          + response.headers());
    }
  }
}
