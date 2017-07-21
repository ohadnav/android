package com.truethat.android.common;

import android.support.test.rule.ActivityTestRule;
import com.google.gson.Gson;
import com.truethat.android.BuildConfig;
import com.truethat.android.application.ApplicationTestUtil;
import com.truethat.android.application.FakeDeviceManager;
import com.truethat.android.application.auth.FakeAuthManager;
import com.truethat.android.application.permissions.FakePermissionsManager;
import com.truethat.android.common.util.CountingDispatcher;
import com.truethat.android.di.component.DaggerTestAppComponent;
import com.truethat.android.di.module.AppModule;
import com.truethat.android.di.module.NetModule;
import com.truethat.android.di.module.fake.FakeAuthModule;
import com.truethat.android.di.module.fake.FakeDeviceModule;
import com.truethat.android.di.module.fake.FakeInternalStorageModule;
import com.truethat.android.di.module.fake.FakePermissionsModule;
import com.truethat.android.di.module.fake.FakeReactionDetectionModule;
import com.truethat.android.empathy.FakeReactionDetectionManager;
import com.truethat.android.model.User;
import com.truethat.android.view.activity.TestActivity;
import okhttp3.mockwebserver.MockWebServer;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.awaitility.core.ThrowingRunnable;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import static com.truethat.android.application.ApplicationTestUtil.getApp;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 15/06/2017 for TrueThat.
 * <p>
 * Base testing suite for instrumentation testing. Initializes mock application modules, and more.
 */

@SuppressWarnings({ "FieldCanBeLocal", "WeakerAccess" }) public class BaseApplicationTestSuite {
  /**
   * Default duration to wait for. When waiting for activities to change for example.
   */
  public static Duration TIMEOUT =
      ApplicationTestUtil.isDebugging() ? Duration.ONE_MINUTE : Duration.ONE_SECOND;
  protected final MockWebServer mMockWebServer = new MockWebServer();
  @Rule public ActivityTestRule<TestActivity> mActivityTestRule =
      new ActivityTestRule<>(TestActivity.class, true, false);
  protected FakePermissionsManager mFakePermissionsManager;
  protected FakeAuthManager mFakeAuthManager;
  protected FakeReactionDetectionManager mFakeReactionDetectionManager;
  protected FakeDeviceManager mFakeDeviceManager;
  protected Gson mGson;
  protected CountingDispatcher mDispatcher;

  @Before public void setUp() throws Exception {
    // Initialize Awaitility
    Awaitility.reset();
    Awaitility.setDefaultTimeout(TIMEOUT);
    // Starts mock server
    mMockWebServer.start(8080);
    setDispatcher(new CountingDispatcher());
    // Injects mock dependencies.
    getApp().updateComponents(DaggerTestAppComponent.builder().appModule(new AppModule(getApp()))
        // Sets the backend URL, for MockWebServer.
        .netModule(new NetModule(BuildConfig.TEST_BASE_BACKEND_URL))
        .fakePermissionsModule(new FakePermissionsModule())
        .fakeAuthModule(new FakeAuthModule())
        .fakeDeviceModule(new FakeDeviceModule())
        .fakeInternalStorageModule(new FakeInternalStorageModule())
        .fakeReactionDetectionModule(new FakeReactionDetectionModule())
        .build());
    // Launches activity
    mActivityTestRule.launchActivity(null);
    // Initializes fakes
    mFakeAuthManager = (FakeAuthManager) mActivityTestRule.getActivity().getAuthManager();
    mFakeDeviceManager = (FakeDeviceManager) mActivityTestRule.getActivity().getDeviceManager();
    mFakePermissionsManager =
        (FakePermissionsManager) mActivityTestRule.getActivity().getPermissionsManager();
    // Sign up
    mFakeAuthManager.signUp(mActivityTestRule.getActivity(),
        new User(mFakeDeviceManager.getDeviceId(), mFakeDeviceManager.getPhoneNumber()));
    mFakeReactionDetectionManager = (FakeReactionDetectionManager) mActivityTestRule.getActivity()
        .getReactionDetectionManager();
    mGson = mActivityTestRule.getActivity().getGson();
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeAuthManager.isAuthOk());
      }
    });
  }

  public void setDispatcher(CountingDispatcher dispatcher) {
    mDispatcher = dispatcher;
    mMockWebServer.setDispatcher(dispatcher);
  }

  @After public void tearDown() throws Exception {
    // Closes mock server
    mMockWebServer.close();
  }
}
