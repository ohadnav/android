package com.truethat.android.application.auth;

import com.truethat.android.BuildConfig;
import com.truethat.android.application.DeviceManager;
import com.truethat.android.application.FakeDeviceManager;
import com.truethat.android.application.storage.internal.FakeInternalStorageManager;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.model.User;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.awaitility.core.ThrowingRunnable;
import org.junit.After;
import org.junit.Before;

import static com.truethat.android.common.network.NetworkUtil.GSON;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 19/07/2017 for TrueThat.
 */

@SuppressWarnings("WeakerAccess") public class AuthManagerTest {
  static final long USER_ID = 1;
  static final DeviceManager DEVICE_MANAGER = new FakeDeviceManager("android1", "+1103874837");
  private static final String FIRST_NAME = "Brad";
  private static final String LAST_NAME = "Pitt";
  final MockWebServer mMockWebServer = new MockWebServer();
  AuthManager mAuthManager;
  FakeInternalStorageManager mInternalStorage;
  TestAuthListener mListener;
  User mUser;

  @Before public void setUp() throws Exception {
    NetworkUtil.setBackendUrl(BuildConfig.TEST_BASE_BACKEND_URL);
    mListener = new TestAuthListener();
    mUser = new User(FIRST_NAME, LAST_NAME, DEVICE_MANAGER.getDeviceId(),
        DEVICE_MANAGER.getPhoneNumber());
    mInternalStorage = new FakeInternalStorageManager();
    // Initialize Awaitility
    Awaitility.reset();
    Awaitility.setDefaultTimeout(Duration.FIVE_HUNDRED_MILLISECONDS);
    Awaitility.setDefaultPollDelay(new Duration(10, TimeUnit.MILLISECONDS));
    Awaitility.setDefaultPollInterval(new Duration(10, TimeUnit.MILLISECONDS));
    // Starts mock server
    mMockWebServer.start(8070);
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        mUser.setId(USER_ID);
        return new MockResponse().setBody(GSON.toJson(mUser));
      }
    });
  }

  @After public void tearDown() throws Exception {
    mMockWebServer.close();
  }

  void performAuth() throws Exception {
    prepareAuth();
    // Authenticate user;
    mAuthManager.auth(mListener);
    assertAuthOk();
  }

  void prepareAuth() throws IOException {
    mUser.setId(USER_ID);
    mInternalStorage.write(AuthManager.LAST_USER_PATH, mUser);
  }

  void assertAuthOk() {
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        // Assert auth-OK
        assertTrue(mAuthManager.isAuthOk());
        // Assert the user was is saved onto internal storage.
        assertEquals(mUser, mInternalStorage.read(AuthManager.LAST_USER_PATH));
        // Assert the current user now has an ID.
        assertEquals(USER_ID, mAuthManager.getCurrentUser().getId().longValue());
        // Assert result is ok.
        assertEquals(AuthResult.OK, mListener.getAuthResult());
      }
    });
  }

  void assertAuthFailed() {
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        // Current user should be null.
        assertNull(mAuthManager.getCurrentUser());
        // Should not be auth-ok
        assertFalse(mAuthManager.isAuthOk());
        // Should have failed result.
        assertEquals(AuthResult.FAILED, mListener.getAuthResult());
      }
    });
  }
}
