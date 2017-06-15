package com.truethat.android.auth;

import android.content.Context;
import android.util.Log;
import com.truethat.android.application.App;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.common.BaseActivity;
import com.truethat.android.common.network.NetworkUtil;
import java.io.IOException;
import retrofit2.Call;
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

  public DefaultAuthModule() {
  }

  public User getUser() {
    return mUser;
  }

  /**
   * Authenticate user synchronously, to ensure a {@link User} can be retrieved from {@link
   * #getUser()}
   *
   * If {@link Permission#PHONE} is not granted then the authentication will terminate and will be
   * run again on {@link BaseActivity#onResume()}
   *
   * @param activity for which to auth.
   */
  @Override public void auth(final BaseActivity activity) {
    boolean authSuccessful = true;
    if (mUser == null) {
      App.getPermissionsModule().requestIfNeeded(activity, Permission.PHONE);
      if (!App.getPermissionsModule().isPermissionGranted(activity, Permission.PHONE)) {
        return;
      }
      try {
        mUser = new User(activity);
      } catch (IOException | ClassNotFoundException e) {
        authSuccessful = false;
        Log.e(TAG, "Could not create user. Sad story.. but it's true.", e);
      }
    }
    // If the user has an ID, don't post an auth request.
    if (mUser != null && !mUser.hasId()) {
      // Sends auth request to the server.
      Call<User> authCall = mAuthAPI.postAuth(mUser);
      try {
        authSuccessful = authSuccessful && handleResponse(authCall, authCall.execute(), activity);
      } catch (IOException e) {
        authSuccessful = false;
        Log.e(TAG, "Authentication request had failed, inconceivable!", e);
      }
    }
    if (!authSuccessful) {
      activity.onAuthFailed();
    }
  }

  /**
   * Handles Auth API HTTP response
   *
   * @param call HTTP request
   * @param response HTTP request. Note that in case of unauthorized users, a null response body is
   * @param context to access internal storage, when updating user.
   * returned.
   * @return whether the response contained a valid {@link User} with an ID.
   */
  private boolean handleResponse(Call<User> call, Response<User> response, Context context)
      throws AssertionError, IOException {
    boolean responseSuccessful = true;
    if (response.isSuccessful() && response.body() != null) {
      User respondedUser = response.body();
      if (respondedUser == null) {
        throw new AssertionError("Responded user is a ghost... scary.");
      }
      mUser.update(respondedUser, context);
    } else {
      Log.e(TAG, "Failed to post event to "
          + call.request().url()
          + "\n"
          + response.code()
          + " "
          + response.message()
          + "\n"
          + response.headers());
      responseSuccessful = false;
    }
    return responseSuccessful;
  }
}
