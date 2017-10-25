package com.truethat.android.view.activity;

import android.content.Intent;
import com.truethat.android.R;
import com.truethat.android.common.BaseInstrumentationTestSuite;
import com.truethat.android.viewmodel.OnBoardingSignUpStageViewModel;
import java.util.concurrent.Callable;
import org.awaitility.core.ThrowingRunnable;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.getCurrentActivity;
import static com.truethat.android.application.ApplicationTestUtil.isKeyboardVisible;
import static com.truethat.android.application.ApplicationTestUtil.waitForActivity;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static com.truethat.android.application.ApplicationTestUtil.withBackgroundColor;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 16/06/2017 for TrueThat.
 */
public class BaseOnBoardingTest extends BaseInstrumentationTestSuite {
  protected static final String NAME = "Matt Damon";
  protected OnBoardingSignUpStageViewModel mSignUpStageViewModel;
  protected OnBoardingActivity mActivity;

  @Override public void setUp() throws Exception {
    super.setUp();
    mFakePermissionsManager.invokeRequestCallback();
  }

  protected void manualSetUp() {
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
    // Grant camera permission
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override public void run() {
        //noinspection ConstantConditions
        mActivity.mHiStageFragment.getView().findViewById(R.id.onBoarding_askButton).performClick();
      }
    });
    // Wait for sign up stage
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(OnBoardingActivity.SIGN_UP_STAGE_INDEX, mActivity.getStageIndex());
      }
    });
    mSignUpStageViewModel = mActivity.mSignUpStageFragment.getViewModel();
    // Type user name and hit done.
    onView(withId(R.id.nameEditText)).perform(typeText(BaseOnBoardingTest.NAME))
        .perform(pressImeActionButton());
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(OnBoardingSignUpStageViewModel.Stage.FINAL, mSignUpStageViewModel.getStage());
      }
    });
    // Wait until detection had started.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mFakeReactionDetectionManager.isSubscribed(mSignUpStageViewModel);
      }
    });
    // Detect smile.
    mFakeReactionDetectionManager.onReactionDetected(
        OnBoardingSignUpStageViewModel.REACTION_FOR_DONE, true);
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(OnBoardingSignUpStageViewModel.Stage.REQUEST_SENT,
            mSignUpStageViewModel.getStage());
      }
    });
  }

  protected void assertSignUpFinalStage() {
    // Should be on sign up stage
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(OnBoardingActivity.SIGN_UP_STAGE_INDEX, mActivity.getStageIndex());
      }
    });
    // Assert detection is ongoing.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isDetecting());
      }
    });
    // Wait until smile text is shown.
    waitMatcher(allOf(isDisplayed(), withId(R.id.finalStageText)));
    // Warning text should be hidden.
    onView(withId(R.id.warningText)).check(matches(not(isDisplayed())));
    // Keyboard should be hidden.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertFalse(isKeyboardVisible());
      }
    });
  }

  protected void assertSigningUpValidName() {
    onView(withId(R.id.nameEditText)).check(matches(withBackgroundColor(
        mTestActivityRule.getActivity()
            .getResources()
            .getColor(OnBoardingSignUpStageViewModel.VALID_NAME_COLOR,
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
    // Detection is NOT ongoing.
    assertFalse(mFakeReactionDetectionManager.isSubscribed(mSignUpStageViewModel));
    // Smile text is hidden
    onView(withId(R.id.finalStageText)).check(matches(not(isDisplayed())));
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