package com.truethat.android.common;

import android.app.Application;
import android.support.test.rule.ActivityTestRule;
import com.truethat.android.application.App;
import com.truethat.android.application.ApplicationTestUtil;
import com.truethat.android.application.auth.MockAuthModule;
import com.truethat.android.application.permissions.MockPermissionsModule;
import com.truethat.android.application.storage.internal.MockInternalStorage;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.util.CountingDispatcher;
import com.truethat.android.di.component.BaseComponent;
import com.truethat.android.di.component.DaggerAppComponent;
import com.truethat.android.di.component.DaggerBaseComponent;
import com.truethat.android.di.module.AppModule;
import com.truethat.android.di.module.NetModule;
import com.truethat.android.empathy.MockReactionDetectionModule;
import com.truethat.android.ui.activity.TestActivity;
import okhttp3.mockwebserver.MockWebServer;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import static com.truethat.android.application.ApplicationTestUtil.getApp;

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
  protected MockPermissionsModule mMockPermissionsModule;
  protected MockAuthModule mMockAuthModule;
  protected MockInternalStorage mMockInternalStorage;
  protected MockReactionDetectionModule mMockReactionDetectionModule;
  protected CountingDispatcher mDispatcher;

  protected BaseComponent mBaseComponent = DaggerBaseComponent.builder()
      .appComponent(
          DaggerAppComponent.builder().appModule(new AppModule(new Application())).build())
      .netModule(new NetModule("http://localhost:8080/"))
      .build();

  @Before public void setUp() throws Exception {
    // Initialize Awaitility
    Awaitility.reset();
    Awaitility.setDefaultTimeout(TIMEOUT);
    // Sets up the mocked permissions module.
    App.setPermissionsModule(mMockPermissionsModule = new MockPermissionsModule());
    // Sets up mocked device manager.
    App.setDeviceManager(new MockDeviceManager());
    // Sets up the mocked auth module.
    App.setAuthModule(mMockAuthModule = new MockAuthModule());
    // Sets up the mocked in-mem internal storage.
    App.setInternalStorage(mMockInternalStorage = new MockInternalStorage());
    // Sets up a mocked emotional reaction detection module.
    App.setReactionDetectionModule(
        mMockReactionDetectionModule = new MockReactionDetectionModule());
    // Sets the backend URL, for MockWebServer.
    NetworkUtil.setBackendUrl("http://localhost:8080/");
    // Starts mock server
    mMockWebServer.start(8080);
    setDispatcher(new CountingDispatcher());
    // Injects mock dependencies.
    getApp().updateBaseComponent(mBaseComponent);
    // Launches activity
    mActivityTestRule.launchActivity(null);
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
