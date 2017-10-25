package com.truethat.android.view.activity;

import android.content.Intent;
import org.junit.Test;

import static com.truethat.android.application.ApplicationTestUtil.getCurrentActivity;
import static com.truethat.android.application.ApplicationTestUtil.waitForActivity;

/**
 * Proudly created by ohad on 16/06/2017 for TrueThat.
 */
public class OnBoardingActivityTest extends BaseOnBoardingTest {

  @Test public void onBoardingFlow() throws Exception {
    manualSetUp();
    doOnBoarding();
    assertOnBoardingSuccessful();
  }

  @Test public void alreadyAuthOk() throws Exception {
    // User should be authenticated.
    // Go to on boarding by mistake.
    mTestActivityRule.getActivity()
        .startActivity(new Intent(mTestActivityRule.getActivity(), OnBoardingActivity.class));
    getCurrentActivity().startActivity(
        new Intent(mTestActivityRule.getActivity(), OnBoardingActivity.class));
    // Should navigate back to Test activity.
    waitForActivity(TestActivity.class);
  }
}