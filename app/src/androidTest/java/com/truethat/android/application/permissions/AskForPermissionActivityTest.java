package com.truethat.android.application.permissions;

import com.truethat.android.R;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.view.activity.AskForPermissionActivity;
import com.truethat.android.view.activity.TestActivity;
import org.junit.Before;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.waitForActivity;

/**
 * Proudly created by ohad on 13/06/2017 for TrueThat.
 */
public class AskForPermissionActivityTest extends BaseApplicationTestSuite {
  private static final Permission PERMISSION = Permission.CAMERA;

  @Before public void setUp() throws Exception {
    super.setUp();
    // Revoke permission on launch.
    mFakePermissionsManager.forbid(PERMISSION);
  }

  @Test public void onRequestPermissionsFailed() throws Exception {
    mActivityTestRule.getActivity().onRequestPermissionsFailed(PERMISSION);
    // Wait until we navigate to ask for permission activity.
    waitForActivity(AskForPermissionActivity.class);
    // Assert that no camera permission fragment is displayed.
    onView(withId(R.id.noCameraPermissionTopRationale)).check(matches(isDisplayed()));
  }

  @Test public void finishIfPermissionIsAlreadyGranted() throws Exception {
    mFakePermissionsManager.grant(PERMISSION);
    mActivityTestRule.getActivity().onRequestPermissionsFailed(PERMISSION);
    // Wait for possible navigation out of test activity, and assert the current activity remains test activity.
    waitForActivity(TestActivity.class);
  }

  @Test public void finishAfterPermissionGranted() throws Exception {
    // Invoke request callback, to finish activity.
    mFakePermissionsManager.setInvokeRequestCallback(true);
    mActivityTestRule.getActivity().onRequestPermissionsFailed(PERMISSION);
    // Wait until we navigate to ask for permission activity.
    waitForActivity(AskForPermissionActivity.class);
    // Grant permission, to mock the scenario where the user allowed the permission.
    mFakePermissionsManager.reset(PERMISSION);
    // Ask for permission again.
    onView(withId(R.id.askPermissionButton)).perform(click());
    // Wait until we return to test activity.
    waitForActivity(TestActivity.class);
  }

  @Test public void askAgainAndDenyDoesNotFinish() throws Exception {
    mActivityTestRule.getActivity().onRequestPermissionsFailed(PERMISSION);
    // Wait until we navigate to ask for permission activity.
    waitForActivity(AskForPermissionActivity.class);
    // Ask for permission again.
    onView(withId(R.id.askPermissionButton)).perform(click());
    // Wait for possible navigation back to test activity, and assert no navigation was performed.
    Thread.sleep(100);
    waitForActivity(AskForPermissionActivity.class);
  }
}