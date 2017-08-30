package com.truethat.android.application.auth;

import java.net.HttpURLConnection;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Test;

import static com.truethat.android.common.network.NetworkUtil.GSON;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 14/06/2017 for TrueThat.
 */
public class BaseAuthManagerTest extends AuthManagerTest {

  @Override public void setUp() throws Exception {
    super.setUp();
    mAuthManager = new BaseAuthManager(DEVICE_MANAGER, mInternalStorage);
  }

  @Test public void alreadyAuthOk() throws Exception {
    performAuth();
    // Authenticate user;
    mAuthManager.auth(mListener);
    assertAuthOk();
    // Should authenticate against backend
    assertEquals(1, mMockWebServer.getRequestCount());
    // Following auth should not send backend requests
    mAuthManager.auth(mListener);
    assertAuthOk();
    assertEquals(1, mMockWebServer.getRequestCount());
  }

  @Test public void authFromLastSession() throws Exception {
    prepareAuth();
    // Authenticate user;
    mAuthManager.auth(mListener);
    assertAuthOk();
    // Should authenticate against backend
    assertEquals(1, mMockWebServer.getRequestCount());
  }

  @Test public void authFromLastSessionFailed() throws Exception {
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED);
      }
    });
    prepareAuth();
    // Authenticate user;
    mAuthManager.auth(mListener);
    assertAuthFailed();
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
    mListener.resetResult();
    mAuthManager.signIn(mListener);
    // Should not authenticate against backend
    assertAuthOk();
    assertEquals(1, mMockWebServer.getRequestCount());
  }

  @Test public void signInByDevice() throws Exception {
    mAuthManager.signIn(mListener);
    // Should make authentication against backend
    assertAuthOk();
    assertEquals(1, mMockWebServer.getRequestCount());
  }

  @Test public void signInByLastSession() throws Exception {
    prepareAuth();
    mAuthManager.signIn(mListener);
    // Should make authentication against backend
    assertAuthOk();
    assertEquals(1, mMockWebServer.getRequestCount());
  }

  @Test public void signInWithStorageFailure() throws Exception {
    prepareAuth();
    mInternalStorage.setShouldFail(true);
    mAuthManager.signIn(mListener);
    // Should make authentication against backend
    assertAuthOk();
    assertEquals(1, mMockWebServer.getRequestCount());
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
    assertAuthOk();
    assertEquals(1, mMockWebServer.getRequestCount());
  }

  @Test public void signOut() throws Exception {
    performAuth();
    mAuthManager.signOut(mListener);
    assertAuthFailed();
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