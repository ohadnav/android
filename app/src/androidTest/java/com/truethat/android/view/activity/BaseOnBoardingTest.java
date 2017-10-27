package com.truethat.android.view.activity;

import android.content.Intent;
import com.truethat.android.R;
import com.truethat.android.common.BaseInstrumentationTestSuite;
import com.truethat.android.viewmodel.OnBoardingCheeseStageViewModel;
import com.truethat.android.viewmodel.OnBoardingSignUpStageViewModel;
import org.awaitility.core.ThrowingRunnable;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.getCurrentActivity;
import static com.truethat.android.application.ApplicationTestUtil.waitForActivity;
import static com.truethat.android.application.ApplicationTestUtil.withBackgroundColor;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 16/06/2017 for TrueThat.
 */
public class BaseOnBoardingTest extends BaseInstrumentationTestSuite {
  protected static final String NAME = "Matt Damon";
  protected OnBoardingActivity mActivity;
  private long mOriginalDetectionDelay = OnBoardingCheeseStageViewModel.sDetectionDelay;
  private long mOriginalFaceLostTimeout = OnBoardingCheeseStageViewModel.sFaceLostTimeout;

  @Override public void setUp() throws Exception {
    super.setUp();
    mFakePermissionsManager.invokeRequestCallback();
    // Shorten delays
    OnBoardingCheeseStageViewModel.sDetectionDelay = 10;
    OnBoardingCheeseStageViewModel.sFaceLostTimeout = 10;
  }

  @Override public void tearDown() throws Exception {
    super.tearDown();
    // Restores original delays.
    OnBoardingCheeseStageViewModel.sDetectionDelay = mOriginalDetectionDelay;
    OnBoardingCheeseStageViewModel.sFaceLostTimeout = mOriginalFaceLostTimeout;
  }

  protected void manualSetUp() {
    doSignOut();
  }

  protected void doSignOut() {
    mFakeAuthManager.signOut(mTestActivityRule.getActivity());
    getCurrentActivity().startActivity(
        new Intent(mTestActivityRule.getActivity(), OnBoardingActivity.class));
    waitForActivity(OnBoardingActivity.class);
    mActivity = (OnBoardingActivity) getCurrentActivity();
  }

  /**
   * Programmatically completes the on boarding process as if a user completed it.
   */
  protected void doOnBoarding() {
    // Should navigate to On-Boarding
    waitForActivity(OnBoardingActivity.class);
    // Should be on first stage
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(OnBoardingActivity.HI_STAGE_INDEX, mActivity.getStageIndex());
      }
    });
    // Grant camera permission
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override public void run() {
        //noinspection ConstantConditions
        mActivity.mHiStageFragment.getView().findViewById(R.id.onBoarding_hiButton).performClick();
      }
    });
    //
    // Wait for cheese stage
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(OnBoardingActivity.CHEESE_STAGE_INDEX, mActivity.getStageIndex());
      }
    });
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(
            mActivity.mCheeseStageFragment.getViewModel()));
      }
    });
    // Proceed to sign up stage
    mFakeReactionDetectionManager.onReactionDetected(
        OnBoardingCheeseStageViewModel.REACTION_FOR_DONE, true);
    // Wait for sign up stage
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(OnBoardingActivity.SIGN_UP_STAGE_INDEX, mActivity.getStageIndex());
      }
    });
    // Type user name and hit done.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        onView(withId(R.id.nameEditText)).perform(typeText(BaseOnBoardingTest.NAME))
            .perform(pressImeActionButton());
      }
    });
  }

  protected void assertSigningUpValidName() {
    onView(withId(R.id.nameEditText)).check(matches(withBackgroundColor(
        mTestActivityRule.getActivity()
            .getResources()
            .getColor(OnBoardingSignUpStageViewModel.VALID_COLOR,
                mTestActivityRule.getActivity().getTheme()))));
  }

  protected void assertSigningUpInvalidName() {
    // Error color indicator is shown.
    onView(withId(R.id.nameEditText)).check(matches(withBackgroundColor(
        mTestActivityRule.getActivity()
            .getResources()
            .getColor(OnBoardingSignUpStageViewModel.ERROR_COLOR,
                mTestActivityRule.getActivity().getTheme()))));
    try {
      Thread.sleep(100);
    } catch (Exception e) {
      assertTrue(false);
    }
  }

  protected void assertOnBoardingSuccessful() {
    // Should navigate back to Test activity.
    waitForActivity(MainActivity.class);
    // Wait until Auth OK.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeAuthManager.isAuthOk());
      }
    });
    // Assert the current user now the proper name.
    assertEquals(NAME, mFakeAuthManager.getCurrentUser().getDisplayName());
  }
}