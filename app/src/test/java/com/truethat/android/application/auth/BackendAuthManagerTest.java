package com.truethat.android.application.auth;

import java.net.HttpURLConnection;
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
import org.junit.Test;

import static com.truethat.android.common.network.NetworkUtil.GSON;
import static org.awaitility.Awaitility.await;

/**
 * Proudly created by ohad on 19/07/2017 for TrueThat.
 */
public class BackendAuthManagerTest extends AuthManagerTest {
  private final MockWebServer mMockWebServer = new MockWebServer();

  @Before public void setUp() throws Exception {
    super.setUp();
    // Initialize async helper.
    Awaitility.reset();
    Awaitility.setDefaultTimeout(Duration.ONE_MINUTE);
    Awaitility.setDefaultPollInterval(new Duration(10, TimeUnit.MILLISECONDS));
    // Starts mock server
    mMockWebServer.start(8070);
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        mUser.setId(USER_ID);
        return new MockResponse().setBody(GSON.toJson(mUser));
      }
    });
    mAuthManager = new BackendAuthManager(DEVICE_MANAGER, mInternalStorage);
  }

  @After public void tearDown() throws Exception {
    mMockWebServer.close();
  }

  @Test public void authOk() throws Exception {
    signIn();
  }

  @Test public void signUp() throws Exception {
    mAuthManager.signUp(mListener, mUser);
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertAuthOk();
      }
    });
  }

  @Test public void badResponse() throws Exception {
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        return new MockResponse();
      }
    });
    mAuthManager.signUp(mListener, mUser);
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertAuthFailed();
      }
    });
  }

  @Test public void badResponseStatus() throws Exception {
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        mUser.setId(USER_ID);
        return new MockResponse().setBody(GSON.toJson(mUser))
            .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
      }
    });
    mAuthManager.signUp(mListener, mUser);
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertAuthFailed();
      }
    });
  }
}