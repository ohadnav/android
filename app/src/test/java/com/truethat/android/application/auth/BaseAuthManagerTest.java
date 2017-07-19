package com.truethat.android.application.auth;

import com.truethat.android.model.User;
import org.junit.Test;

/**
 * Proudly created by ohad on 14/06/2017 for TrueThat.
 */
public class BaseAuthManagerTest extends AuthManagerTest {

  @Override public void setUp() throws Exception {
    super.setUp();
    mAuthManager = new BaseAuthManager(DEVICE_MANAGER, mInternalStorage) {
      @Override protected void requestAuth(AuthListener listener, User user) {
        try {
          mUser.setId(USER_ID);
          handleSuccessfulResponse(mUser);
          listener.onAuthOk();
        } catch (Exception e) {
          listener.onAuthFailed();
        }
      }
    };
  }

  @Test public void authOk() throws Exception {
    signIn();
  }

  @Test public void authFailed() throws Exception {
    mAuthManager.auth(mListener);
    assertAuthFailed();
  }

  @Test public void authWithStorageFailure() throws Exception {
    prepareAuth();
    mInternalStorage.setShouldFail(true);
    mAuthManager.auth(mListener);
    assertAuthFailed();
  }

  @Test public void signInAlreadyAuthOk() throws Exception {
    signIn();
    mListener.resetResult();
    mAuthManager.signIn(mListener);
    assertAuthOk();
  }

  @Test public void signInByDevice() throws Exception {
    mAuthManager.signIn(mListener);
    assertAuthOk();
  }

  @Test public void signUpAlreadyAuthOk() throws Exception {
    signIn();
    mListener.resetResult();
    mAuthManager.signUp(mListener, mUser);
    assertAuthOk();
  }

  @Test public void signUp() throws Exception {
    mAuthManager.signUp(mListener, mUser);
    assertAuthOk();
  }

  @Test public void signOut() throws Exception {
    signIn();
    mAuthManager.signOut(mListener);
    assertAuthFailed();
  }
}