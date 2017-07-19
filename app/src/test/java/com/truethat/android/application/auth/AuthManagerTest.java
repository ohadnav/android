package com.truethat.android.application.auth;

import com.truethat.android.application.DeviceManager;
import com.truethat.android.application.FakeDeviceManager;
import com.truethat.android.application.storage.internal.FakeInternalStorageManager;
import com.truethat.android.model.User;
import java.io.IOException;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 19/07/2017 for TrueThat.
 */

class AuthManagerTest {
  static final long USER_ID = 1;
  static final DeviceManager DEVICE_MANAGER = new FakeDeviceManager("android1", "911-kapara");
  private static final String FIRST_NAME = "Brad";
  private static final String LAST_NAME = "Pitt";
  AuthManager mAuthManager;
  FakeInternalStorageManager mInternalStorage;
  TestAuthListener mListener;
  User mUser;

  @Before public void setUp() throws Exception {
    mListener = new TestAuthListener();
    mUser = new User(FIRST_NAME, LAST_NAME, DEVICE_MANAGER.getDeviceId(),
        DEVICE_MANAGER.getPhoneNumber());
    mInternalStorage = new FakeInternalStorageManager();
  }

  void signIn() throws Exception {
    prepareAuth();
    // Authenticate user;
    mAuthManager.auth(mListener);
    assertAuthOk();
  }

  void prepareAuth() throws IOException {
    mUser.setId(USER_ID);
    mInternalStorage.write(AuthManager.LAST_USER_PATH, mUser);
  }

  void assertAuthOk() throws Exception {
    // Assert the user was is saved onto internal storage.
    assertEquals(mUser, mInternalStorage.read(AuthManager.LAST_USER_PATH));
    // Assert the current user now has an ID.
    assertEquals(USER_ID, mAuthManager.currentUser().getId());
    // Assert auth-OK
    assertTrue(mAuthManager.isAuthOk());
    // Assert result is ok.
    assertEquals(AuthResult.OK, mListener.getAuthResult());
  }

  void assertAuthFailed() {
    // Current user should be null.
    assertNull(mAuthManager.currentUser());
    // Should not be auth-ok
    assertFalse(mAuthManager.isAuthOk());
    // Should have failed result.
    assertEquals(AuthResult.FAILED, mListener.getAuthResult());
  }
}
