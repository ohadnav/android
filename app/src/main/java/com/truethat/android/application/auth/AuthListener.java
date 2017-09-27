package com.truethat.android.application.auth;

import com.truethat.android.viewmodel.viewinterface.BaseListener;

/**
 * Proudly created by ohad on 18/07/2017 for TrueThat.
 */

public interface AuthListener extends BaseListener {
  /**
   * Invoked after a successful authentication.
   */
  void onAuthOk();

  /**
   * Invoked after a failed auth attempt, or following a sign out.
   */
  void onAuthFailed();
}
