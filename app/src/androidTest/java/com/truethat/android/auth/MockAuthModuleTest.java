package com.truethat.android.auth;

import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.BaseApplicationTest;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 15/06/2017 for TrueThat.
 */
public class MockAuthModuleTest extends BaseApplicationTest {

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
    onView(isRoot()).perform(waitMatcher(allOf(isDisplayed(), withId(R.id.onBoardingActivity)),
        TimeUnit.SECONDS.toMillis(1)));
    // Assert auth status is not OK
    assertFalse(App.getAuthModule().isAuthOk());
  }

  private void assertSuccessfulAuth() {
    // Should navigate back to Test activity.
    onView(withId(R.id.testActivity)).check(matches(isDisplayed()));
    // Assert auth status is OK
    assertTrue(App.getAuthModule().isAuthOk());
    // User exists
    assertNotNull(mMockAuthModule.getUser());
  }

  private void assertFailedAuth() {
    // Should navigate to Welcome activity.
    onView(isRoot()).perform(waitMatcher(allOf(isDisplayed(), withId(R.id.welcomeActivity)),
        TimeUnit.SECONDS.toMillis(1)));
    // Assert auth status is not OK
    assertFalse(App.getAuthModule().isAuthOk());
  }
}