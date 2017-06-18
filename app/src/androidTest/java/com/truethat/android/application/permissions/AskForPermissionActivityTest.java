package com.truethat.android.application.permissions;

import com.truethat.android.R;
import com.truethat.android.common.BaseApplicationTest;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static org.hamcrest.Matchers.allOf;

/**
 * Proudly created by ohad on 13/06/2017 for TrueThat.
 */
public class AskForPermissionActivityTest extends BaseApplicationTest {
  private static final Permission PERMISSION = Permission.CAMERA;

  @Before public void setUp() throws Exception {
    super.setUp();
    // Revoke permission on launch.
    mMockPermissionsModule.forbid(PERMISSION);
  }

  @Test public void onRequestPermissionsFailed() throws Exception {
    mActivityTestRule.getActivity().onRequestPermissionsFailed(PERMISSION);
    // Wait until we navigate to ask for permission activity.
    onView(isRoot()).perform(
        waitMatcher(allOf(isDisplayed(), withId(R.id.askForPermissionActivity)),
            TimeUnit.SECONDS.toMillis(1)));
    // Assert that no camera permission fragment is displayed.
    onView(withId(R.id.noCameraPermissionImage)).check(matches(isDisplayed()));
    onView(withId(R.id.noCameraPermissionTopRationale)).check(matches(isDisplayed()));
  }

  @Test public void finishIfPermissionIsAlreadyGranted() throws Exception {
    mMockPermissionsModule.grant(PERMISSION);
    mActivityTestRule.getActivity().onRequestPermissionsFailed(PERMISSION);
    // Wait for possible navigation out of test activity, and assert the current activity remains test activity.
    Thread.sleep(100);
    onView(withId(R.id.testActivity)).check(matches(isDisplayed()));
  }

  @Test public void finishAfterPermissionGranted() throws Exception {
    mActivityTestRule.getActivity().onRequestPermissionsFailed(PERMISSION);
    // Wait until we navigate to ask for permission activity.
    onView(isRoot()).perform(
        waitMatcher(allOf(isDisplayed(), withId(R.id.askForPermissionActivity)),
            TimeUnit.SECONDS.toMillis(3)));
    // Grant permission, to mock the scenario where the user allowed the permission.
    mMockPermissionsModule.reset(PERMISSION);
    // Ask for permission again.
    onView(withId(R.id.askPermissionButton)).perform(click());
    // Wait until we return to test activity.
    onView(isRoot()).perform(
        waitMatcher(allOf(isDisplayed(), withId(R.id.testActivity)), TimeUnit.SECONDS.toMillis(1)));
  }

  @Test public void askAgainAndDenyDoesNotFinish() throws Exception {
    mActivityTestRule.getActivity().onRequestPermissionsFailed(PERMISSION);
    // Wait until we navigate to ask for permission activity.
    onView(isRoot()).perform(
        waitMatcher(allOf(isDisplayed(), withId(R.id.askForPermissionActivity)),
            TimeUnit.SECONDS.toMillis(3)));
    // Ask for permission again.
    onView(withId(R.id.askPermissionButton)).perform(click());
    // Wait for possible navigation back to test activity, and assert no navigation was performed.
    Thread.sleep(100);
    onView(withId(R.id.askForPermissionActivity)).check(matches(isDisplayed()));
  }
}