package com.truethat.android.common;

import android.support.test.rule.ActivityTestRule;
import com.truethat.android.application.App;
import com.truethat.android.application.ApplicationTestUtil;
import com.truethat.android.application.permissions.MockPermissionsModule;
import com.truethat.android.application.storage.internal.MockInternalStorage;
import com.truethat.android.auth.MockAuthModule;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.empathy.MockReactionDetectionModule;
import com.truethat.android.ui.common.TestActivity;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import static com.truethat.android.BuildConfig.PORT;

/**
 * Proudly created by ohad on 15/06/2017 for TrueThat.
 *
 * BaseApplicationTestSuite suite. Initializes mock application modules, and more.
 */

@SuppressWarnings({ "FieldCanBeLocal", "WeakerAccess" }) public class BaseApplicationTestSuite {
  /**
   * Default duration to wait for. When waiting for activities to change for example.
   */
  public static final Duration DEFAULT_TIMEOUT =
      ApplicationTestUtil.isDebugging() ? Duration.ONE_MINUTE : Duration.ONE_SECOND;
  protected final MockWebServer mMockWebServer = new MockWebServer();
  @Rule public ActivityTestRule<TestActivity> mActivityTestRule =
      new ActivityTestRule<>(TestActivity.class, true, false);
  protected MockPermissionsModule mMockPermissionsModule;
  protected MockAuthModule mMockAuthModule;
  protected MockInternalStorage mMockInternalStorage;
  protected MockReactionDetectionModule mMockReactionDetectionModule;

  @Before public void setUp() throws Exception {
    // Initialize Awaitility
    Awaitility.reset();
    Awaitility.setDefaultTimeout(DEFAULT_TIMEOUT);
    // Sets up the mocked permissions module.
    App.setPermissionsModule(mMockPermissionsModule = new MockPermissionsModule());
    // Sets up the mocked auth module.
    App.setAuthModule(mMockAuthModule = new MockAuthModule());
    // Sets up the mocked in-mem internal storage.
    App.setInternalStorage(mMockInternalStorage = new MockInternalStorage());
    // Sets up a mocked emotional reaction detection module.
    App.setReactionDetectionModule(
        mMockReactionDetectionModule = new MockReactionDetectionModule());
    // Sets the backend URL, for MockWebServer.
    NetworkUtil.setBackendUrl("http://localhost");
    // Starts mock server
    mMockWebServer.start(PORT);
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        return new MockResponse();
      }
    });
    // Launches activity
    mActivityTestRule.launchActivity(null);
  }

  @After public void tearDown() throws Exception {
    // Closes mock server
    mMockWebServer.close();
  }
}
