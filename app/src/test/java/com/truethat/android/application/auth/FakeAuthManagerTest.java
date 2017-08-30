package com.truethat.android.application.auth;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Proudly created by ohad on 18/07/2017 for TrueThat.
 */
public class FakeAuthManagerTest extends AuthManagerTest {
  @Before public void setUp() throws Exception {
    super.setUp();
    mAuthManager = new FakeAuthManager(DEVICE_MANAGER, mInternalStorage);
  }

  @Test public void authOkFake() throws Exception {
    performAuth();
    assertEquals(0, mMockWebServer.getRequestCount());
  }

  @Test public void authRegular() throws Exception {
    ((FakeAuthManager) mAuthManager).setUseNetwork(true);
    performAuth();
    assertEquals(1, mMockWebServer.getRequestCount());
  }

  @Test public void signUpFake() throws Exception {
    mAuthManager.signUp(mListener, mUser);
    assertAuthOk();
    assertEquals(0, mMockWebServer.getRequestCount());
  }

  @Test public void disallowAuth() throws Exception {
    ((FakeAuthManager) mAuthManager).setAllowAuth(false);
    prepareAuth();
    // Authenticate user;
    mAuthManager.auth(mListener);
    // Should not be auth-ok
    assertFalse(mAuthManager.isAuthOk());
  }
}