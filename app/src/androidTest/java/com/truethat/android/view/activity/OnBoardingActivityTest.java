package com.truethat.android.view.activity;

import android.content.Intent;
import android.widget.EditText;
import com.truethat.android.R;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.viewmodel.OnBoardingViewModel;
import java.util.concurrent.Callable;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Before;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.getCurrentActivity;
import static com.truethat.android.application.ApplicationTestUtil.isKeyboardVisible;
import static com.truethat.android.application.ApplicationTestUtil.waitForActivity;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static com.truethat.android.application.ApplicationTestUtil.withBackgroundColor;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 16/06/2017 for TrueThat.
 */
public class OnBoardingActivityTest extends BaseApplicationTestSuite {
  private static final String NAME = "Matt Damon";
  private OnBoardingViewModel mViewModel;

  /**
   * Programmatically completes the on boarding process as if a user completed it.
   *  @param name                     of the new user.
   *
   */
  private void doOnBoarding(String name) {
    // Should navigate to On-Boarding
    waitForActivity(OnBoardingActivity.class);
    // Type user name and hit done.
    onView(withId(R.id.nameEditText)).perform(typeText(name)).perform(pressImeActionButton());
    // Wait until detection had started.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mFakeReactionDetectionManager.isSubscribed(mViewModel);
      }
    });
    // Detect smile.
    mFakeReactionDetectionManager.doDetection(OnBoardingViewModel.REACTION_FOR_DONE);
    // Wait until Auth OK.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeAuthManager.isAuthOk());
      }
    });
  }

  @Before public void setUp() throws Exception {
    super.setUp();
    // Signs out
    mFakeAuthManager.signOut(mActivityTestRule.getActivity());
    getCurrentActivity().startActivity(
        new Intent(mActivityTestRule.getActivity(), OnBoardingActivity.class));
    waitForActivity(OnBoardingActivity.class);
    OnBoardingActivity activity = (OnBoardingActivity) getCurrentActivity();
    mViewModel = activity.getViewModel();
  }

  @Test public void successfulOnBoarding() throws Exception {
    // EditText should be auto focused.
    onView(withId(R.id.nameEditText)).check(matches(hasFocus()));
    doOnBoarding(NAME);
    assertOnBoardingSuccessful();
  }

  @Test public void alreadyAuthOk() throws Exception {
    doOnBoarding(NAME);
    assertOnBoardingSuccessful();
    // Go to on boarding by mistake.
    mActivityTestRule.getActivity()
        .startActivity(new Intent(mActivityTestRule.getActivity(), OnBoardingActivity.class));
    // Should navigate back to test activity.
    waitForActivity(TestActivity.class);
  }

  private void assertOnBoardingSuccessful() {
    // Should navigate back to Test activity.
    waitForActivity(TestActivity.class);
    // Wait until Auth OK.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeAuthManager.isAuthOk());
      }
    });
    // Assert the current user now the proper name.
    assertEquals(NAME, mFakeAuthManager.currentUser().getDisplayName());
  }
}