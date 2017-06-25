package com.truethat.android.auth;

import com.truethat.android.application.App;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.ui.common.TestActivity;
import com.truethat.android.ui.welcome.OnBoardingActivity;
import com.truethat.android.ui.welcome.WelcomeActivity;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Before;
import org.junit.Test;

import static com.truethat.android.application.ApplicationTestUtil.waitForActivity;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 15/06/2017 for TrueThat.
 */
public class MockAuthModuleTest extends BaseApplicationTestSuite {

  @Before public void setUp() throws Exception {
    super.setUp();
    mMockAuthModule.setAllowAuth(true);
  }

  @Test public void auth() throws Exception {
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    assertSuccessfulAuth();
  }

  @Test public void authNotAllowed() throws Exception {
    mMockAuthModule.setAllowAuth(false);
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    assertFailedAuth();
  }

  @Test public void signOut() throws Exception {
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    assertSuccessfulAuth();
    // Sign out
    mMockAuthModule.setAllowAuth(false);
    // Assert auth status is not OK
    assertFalse(App.getAuthModule().isAuthOk());
  }

  @Test public void signOutByUndoOnBoarding() throws Exception {
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    assertSuccessfulAuth();
    // Sign out
    mMockAuthModule.setOnBoarded(false);
    // Assert auth status is not OK
    assertFalse(App.getAuthModule().isAuthOk());
  }

  @Test public void onBoarding() throws Exception {
    mMockAuthModule.setOnBoarded(false);
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    // Should navigate to OnBoarding activity.
    waitForActivity(OnBoardingActivity.class);
    // Assert auth status is not OK
    assertFalse(App.getAuthModule().isAuthOk());
  }

  private void assertSuccessfulAuth() {
    // Should navigate back to Test activity.
    waitForActivity(TestActivity.class);
    // Assert auth status is OK
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(App.getAuthModule().isAuthOk());
      }
    });
    // User exists
    assertNotNull(mMockAuthModule.getUser());
  }

  private void assertFailedAuth() {
    // Should navigate to Welcome activity.
    waitForActivity(WelcomeActivity.class);
    // Assert auth status is not OK
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertFalse(App.getAuthModule().isAuthOk());
      }
    });
  }
}