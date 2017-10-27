package com.truethat.android.view.fragment;

import com.truethat.android.R;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.common.BaseInstrumentationTestSuite;
import com.truethat.android.view.activity.BaseOnBoardingTest;
import com.truethat.android.view.activity.OnBoardingActivity;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 25/10/2017 for TrueThat.
 */
public class OnBoardingHiStageFragmentTest extends BaseOnBoardingTest {

  @Test public void cameraPermissionGranted() throws Exception {
    manualSetUp();
    // Grant camera permission
    onView(withId(R.id.onBoarding_hiButton)).perform(click());
    // Wait for next stage
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(OnBoardingActivity.HI_STAGE_INDEX + 1, mActivity.getStageIndex());
      }
    });
  }

  @Test public void cameraPermissionAlreadyGranted() throws Exception {
    mFakePermissionsManager.grant(Permission.CAMERA);
    manualSetUp();
    // Should still wait for click
    Thread.sleep(Math.min(BaseInstrumentationTestSuite.TIMEOUT.getValueInMS() / 2, 500));
    assertEquals(OnBoardingActivity.HI_STAGE_INDEX, mActivity.getStageIndex());
    // Click for next stage
    onView(withId(R.id.onBoarding_hiButton)).perform(click());
    // Wait for next stage
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(OnBoardingActivity.HI_STAGE_INDEX + 1, mActivity.getStageIndex());
      }
    });
  }
}