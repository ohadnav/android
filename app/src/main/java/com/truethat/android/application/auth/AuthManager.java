package com.truethat.android.application.auth;

import com.truethat.android.application.DeviceManager;
import com.truethat.android.model.User;

/**
 * Proudly created by ohad on 29/05/2017 for TrueThat.
 */

public interface AuthManager {
  String TAG = AuthManager.class.getSimpleName();
  /**
   * User session path, within the app internal storage.
   */
  String LAST_USER_PATH = "users/last.user";

  User getCurrentUser();

  /**
   * Retrieve last session from internal storage.
   *
   * @param listener to apply auth callbacks.
   */
  void auth(AuthListener listener);

  /**
   * Authenticates against our backend, based on {@link DeviceManager} and {@link
   * #getCurrentUser()}.
   *
   * @param listener to apply auth callbacks.
   */
  void signIn(AuthListener listener);

  /**
   * Signs a new {@link User} up, and registers that user on our petite backend. Upon successful
   * registration the {@link AuthListener#onAuthOk()} callback is invoked.
   *
   * @param listener to apply auth callbacks.
   * @param newUser  to sign up.
   */
  void signUp(AuthListener listener, User newUser);

  /**
   * @return Whether the {@link #getCurrentUser()} is authorized.
   */
  boolean isAuthOk();

  /**
   * Signing {@link #getCurrentUser()} out.
   *
   * @param listener to apply auth callbacks.
   */
  void signOut(AuthListener listener);

  /**
   * Cancels current auth requests.
   */
  void cancelRequest();
}
