package com.truethat.android.application.auth;

import com.google.gson.Gson;
import com.truethat.android.BuildConfig;
import com.truethat.android.di.module.NetModule;
import java.net.HttpURLConnection;
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
import retrofit2.Retrofit;

import static org.awaitility.Awaitility.await;

/**
 * Proudly created by ohad on 19/07/2017 for TrueThat.
 */
public class DefaultAuthManagerTest extends AuthManagerTest {
  private static final NetModule NET_MODULE = new NetModule(BuildConfig.EMULATOR_BASE_BACKEND_URL);
  private static final Gson GSON = NET_MODULE.provideGson();
  private static final Retrofit RETROFIT =
      NET_MODULE.provideRetrofit(GSON, NET_MODULE.provideOkHttpClient());
  private final MockWebServer mMockWebServer = new MockWebServer();

  @Before public void setUp() throws Exception {
    super.setUp();
    // Initialize async helper.
    Awaitility.reset();
    Awaitility.setDefaultTimeout(Duration.ONE_SECOND);
    // Starts mock server
    mMockWebServer.start(8080);
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        mUser.setId(USER_ID);
        return new MockResponse().setBody(GSON.toJson(mUser));
      }
    });
    mAuthManager = new DefaultAuthManager(DEVICE_MANAGER, mInternalStorage, RETROFIT);
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