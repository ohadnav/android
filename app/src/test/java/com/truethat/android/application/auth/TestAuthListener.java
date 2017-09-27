package com.truethat.android.application.auth;

/**
 * Proudly created by ohad on 18/07/2017 for TrueThat.
 */

class TestAuthListener implements AuthListener {
  private AuthResult mAuthResult;

  @Override public void onAuthOk() {
    mAuthResult = AuthResult.OK;
  }

  @Override public void onAuthFailed() {
    mAuthResult = AuthResult.FAILED;
  }

  @Override public String getTAG() {
    return this.getClass().getSimpleName();
  }

  void resetResult() {
    mAuthResult = null;
  }

  AuthResult getAuthResult() {
    return mAuthResult;
  }
}
