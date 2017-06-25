package com.truethat.android.ui.welcome;

import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.BaseApplicationTestSuite;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.waitForActivity;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 16/06/2017 for TrueThat.
 */
public class WelcomeActivityTest extends BaseApplicationTestSuite {
  @Test public void onAuthFailed() throws Exception {
    // Should navigate to welcome activity
    mMockAuthModule.setAllowAuth(false);
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    waitForActivity(WelcomeActivity.class);
    // Error text is visible
    onView(withId(R.id.errorText)).check(matches(isDisplayed()));
    // Sign in text is visible
    onView(withId(R.id.signInText)).check(matches(isDisplayed()));
  }

  @Test public void onBoarding() throws Exception {
    mMockAuthModule.setAllowAuth(false);
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    onView(withId(R.id.joinLayout)).check(matches(isDisplayed())).perform(click());
    // Should navigate to on boarding
    waitForActivity(OnBoardingActivity.class);
  }

  @Test public void userInitiatedAuth() throws Exception {
    // Should navigate to welcome activity
    mMockAuthModule.setAllowAuth(false);
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    waitForActivity(WelcomeActivity.class);
    // Allow auth now.
    mMockAuthModule.setAllowAuth(true);
    // Try again by clicking on sign in text
    onView(withId(R.id.signInText)).check(matches(isDisplayed())).perform(click());
    // Should be signed in.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(App.getAuthModule().isAuthOk());
      }
    });
  }
}