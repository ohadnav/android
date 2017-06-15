package com.truethat.android.auth;

import android.support.test.rule.ActivityTestRule;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.util.TestActivity;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertNotNull;

/**
 * Proudly created by ohad on 15/06/2017 for TrueThat.
 */
public class MockAuthModuleTest {
  @Rule public ActivityTestRule<TestActivity> mActivityTestRule =
      new ActivityTestRule<>(TestActivity.class, true, false);
  private MockAuthModule mMockAuthModule;

  @AfterClass public static void afterClass() throws Exception {
    // Restores default auth module.
    App.setAuthModule(new DefaultAuthModule());
  }

  @Before public void setUp() throws Exception {
    // Resets mock auth module.
    mMockAuthModule = new MockAuthModule();
    // Sets mock auth module.
    App.setAuthModule(mMockAuthModule);
    // Launches activity
    mActivityTestRule.launchActivity(null);
  }

  @Test public void getUser() throws Exception {
    assertNotNull(mMockAuthModule.getUser());
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

  private void assertSuccessfulAuth() {
    // Should navigate back to Test activity.
    onView(withId(R.id.testActivity)).check(matches(isDisplayed()));
  }

  private void assertFailedAuth() {
    // Should navigate to Welcome activity.
    onView(isRoot()).perform(waitMatcher(allOf(isDisplayed(), withId(R.id.welcomeActivity)),
        TimeUnit.SECONDS.toMillis(1)));
  }
}