package com.truethat.android.view.activity;

import android.support.test.rule.ActivityTestRule;
import com.truethat.android.R;
import com.truethat.android.common.BaseInstrumentationTestSuite;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.waitForActivity;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 16/06/2017 for TrueThat.
 */
public class WelcomeActivityTest extends BaseInstrumentationTestSuite {
  @Rule public ActivityTestRule<WelcomeActivity> mWelcomeActivityTestRule =
      new ActivityTestRule<>(WelcomeActivity.class, true, false);

  @Test public void onAuthFailed() throws Exception {
    // Sign out
    mFakeAuthManager.signOut(mTestActivityRule.getActivity());
    // Launch Welcome activity with auth ok.
    mWelcomeActivityTestRule.launchActivity(null);
    mWelcomeActivityTestRule.getActivity().onAuthFailed();
    // Dialog is visible
    waitMatcher(allOf(withId(R.id.welcomeDialog_button), isDisplayed()));
  }

  @Test public void alreadyAuthOk() throws Exception {
    // Launch Welcome activity with auth ok.
    mWelcomeActivityTestRule.launchActivity(null);
    // Should navigate to main activity
    waitForActivity(MainActivity.class);
  }

  @Test public void onBoarding() throws Exception {
    // Sign out
    mFakeAuthManager.signOut(mTestActivityRule.getActivity());
    mWelcomeActivityTestRule.launchActivity(null);
    onView(withId(R.id.welcome_join)).check(matches(isDisplayed())).perform(click());
    // Should navigate to on boarding
    waitForActivity(OnBoardingActivity.class);
  }

  @Test public void userInitiatedAuth() throws Exception {
    // Sign out
    mFakeAuthManager.signOut(mTestActivityRule.getActivity());
    mWelcomeActivityTestRule.launchActivity(null);
    // Try again by clicking on sign in text
    onView(withId(R.id.welcome_signIn)).check(matches(isDisplayed())).perform(click());
    // Should be signed in.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeAuthManager.isAuthOk());
      }
    });
  }
}