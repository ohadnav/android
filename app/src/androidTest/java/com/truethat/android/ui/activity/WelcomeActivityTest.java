package com.truethat.android.ui.activity;

import android.support.test.filters.FlakyTest;
import com.truethat.android.R;
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

  @Override public void setUp() throws Exception {
    super.setUp();
    // Signs out
    mFakeAuthManager.signOut(mActivityTestRule.getActivity());
    // Should navigate to welcome activity
    waitForActivity(WelcomeActivity.class);
  }

  @Test public void onAuthFailed() throws Exception {
    // Error text is visible
    onView(withId(R.id.errorText)).check(matches(isDisplayed()));
  }

  @Test @FlakyTest public void onBoarding() throws Exception {
    onView(withId(R.id.joinLayout)).check(matches(isDisplayed())).perform(click());
    // Should navigate to on boarding
    waitForActivity(OnBoardingActivity.class);
  }

  @Test public void userInitiatedAuth() throws Exception {
    // Try again by clicking on sign in text
    onView(withId(R.id.signInText)).check(matches(isDisplayed())).perform(click());
    // Should be signed in.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeAuthManager.isAuthOk());
      }
    });
  }
}