package com.truethat.android.application.auth;

import android.content.Context;
import android.util.Log;
import com.truethat.android.application.App;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.common.network.AuthAPI;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.util.BackgroundHandler;
import com.truethat.android.model.User;
import com.truethat.android.ui.activity.BaseActivity;
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
  /**
   * Used for network requests.
   */
  private BackgroundHandler mNetworkHandler =
      new BackgroundHandler(this.getClass().getSimpleName());

  public DefaultAuthModule() {
  }

  public User getUser() {
    return mUser;
  }

  /**
   * Authenticate user synchronously, to ensure a {@link User} can be retrieved from {@link
   * #getUser()}
   * <p>
   * If {@link Permission#PHONE} is not granted then the authentication will terminate and will be
   * run again on {@link BaseActivity#onResume()}
   *
   * @param activity for which to auth.
   */
  @Override public void auth(final BaseActivity activity) {
    // Create a new user instance, if needed.
    if (mUser == null) {
      App.getPermissionsModule().requestIfNeeded(activity, Permission.PHONE);
      if (!App.getPermissionsModule().isPermissionGranted(activity, Permission.PHONE)) {
        Log.i(TAG, "No phone permission, stopping auth.");
        // No phone permission, stop here, to let ask for permission activity gain control.
        return;
      }
      try {
        mUser = new User(activity);
      } catch (IOException | ClassNotFoundException e) {
        Log.e(TAG, "Could not create user. Sad story.. but it's true.", e);
        activity.runOnUiThread(new Runnable() {
          @Override public void run() {
            activity.onAuthFailed();
          }
        });
      }
    }
    if (mUser != null) {
      if (!mUser.onBoarded()) {
        // User not on-boarded yet.
        activity.runOnUiThread(new Runnable() {
          @Override public void run() {
            activity.onBoarding();
          }
        });
      } else if (!mUser.hasId()) {
        // ID is missing, get one from server.
        backendAuth(activity);
      } else if (isAuthOk()) {
        // Already authenticated
        activity.runOnUiThread(new Runnable() {
          @Override public void run() {
            activity.onAuthOk();
          }
        });
      } else {
        Log.e(TAG, "Unknown auth error.");
        activity.runOnUiThread(new Runnable() {
          @Override public void run() {
            activity.onAuthFailed();
          }
        });
      }
    }
  }

  @Override public boolean isAuthOk() {
    return mUser != null && mUser.isAuthOk();
  }

  @Override public void signOut() {
    Log.v(TAG, "signing " + (mUser != null ? mUser.getDisplayName() : "") + " out...");
    mUser = null;
  }

  /**
   * Authenticates user against our lovely backend
   *
   * @param activity to use for auth callbacks
   */
  private void backendAuth(final BaseActivity activity) {
    mNetworkHandler.start();
    final Call<User> authCall = mAuthAPI.postAuth(mUser);
    if (!mNetworkHandler.getHandler().post(new Runnable() {
      @Override public void run() {
        try {
          if (handleResponse(authCall, authCall.execute(), activity)) {
            activity.runOnUiThread(new Runnable() {
              @Override public void run() {
                activity.onAuthOk();
              }
            });
          }
        } catch (IOException | AssertionError e) {
          // Auth had failed
          Log.e(TAG, "Authentication request had failed, inconceivable!", e);
          activity.runOnUiThread(new Runnable() {
            @Override public void run() {
              activity.onAuthFailed();
            }
          });
        }
      }
    })) {
      // Auth had failed
      Log.e(TAG, "Network thread handler failure.");
      activity.runOnUiThread(new Runnable() {
        @Override public void run() {
          activity.onAuthFailed();
        }
      });
    }
    mNetworkHandler.stop();
  }

  /**
   * Handles Auth API HTTP response
   *
   * @param call     HTTP request
   * @param response HTTP request. Note that in case of unauthorized users, a null response body is
   * @param context  to access internal storage, when updating user.
   *                 returned.
   *
   * @return whether the response contained a valid {@link User} with an ID.
   */
  private boolean handleResponse(Call<User> call, Response<User> response, Context context)
      throws AssertionError, IOException {
    boolean responseSuccessful = true;
    if (response.isSuccessful()) {
      User respondedUser = response.body();
      if (respondedUser == null) {
        throw new AssertionError("Responded user is a ghost... scary.");
      }
      mUser.update(respondedUser, context);
      Log.v(TAG, mUser.getDisplayName() + " is authenticated.");
    } else {
      Log.e(TAG, "Failed auth request "
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
