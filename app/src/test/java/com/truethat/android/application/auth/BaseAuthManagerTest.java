package com.truethat.android.application.auth;

import com.truethat.android.model.User;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 14/06/2017 for TrueThat.
 */
public class BaseAuthManagerTest extends AuthManagerTest {
  private boolean mPerformedBackendCall;

  @Override public void setUp() throws Exception {
    super.setUp();
    mPerformedBackendCall = false;
    mAuthManager = new BaseAuthManager(DEVICE_MANAGER, mInternalStorage) {
      @Override protected void requestAuth(AuthListener listener, User user) {
        mPerformedBackendCall = true;
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

  @Test public void alreadyAuthOk() throws Exception {
    performAuth();
    mPerformedBackendCall = false;
    // Authenticate user;
    mAuthManager.auth(mListener);
    assertAuthOk();
    // Should not authenticate against backend
    assertFalse(mPerformedBackendCall);
  }

  @Test public void authFromStorage() throws Exception {
    prepareAuth();
    // Authenticate user;
    mAuthManager.auth(mListener);
    assertAuthOk();
    // Should authenticate against backend
    assertTrue(mPerformedBackendCall);
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
    performAuth();
    mPerformedBackendCall = false;
    mListener.resetResult();
    mAuthManager.signIn(mListener);
    // Should not authenticate against backend
    assertFalse(mPerformedBackendCall);
    assertAuthOk();
  }

  @Test public void signInByDevice() throws Exception {
    mAuthManager.signIn(mListener);
    // Should make authentication against backend
    assertTrue(mPerformedBackendCall);
    assertAuthOk();
  }

  @Test public void signUpAlreadyAuthOk() throws Exception {
    performAuth();
    mListener.resetResult();
    mAuthManager.signUp(mListener, mUser);
    assertAuthOk();
  }

  @Test public void signUp() throws Exception {
    mAuthManager.signUp(mListener, mUser);
    // Should make authentication against backend
    assertTrue(mPerformedBackendCall);
    assertAuthOk();
  }

  @Test public void signOut() throws Exception {
    performAuth();
    mAuthManager.signOut(mListener);
    assertAuthFailed();
  }
}