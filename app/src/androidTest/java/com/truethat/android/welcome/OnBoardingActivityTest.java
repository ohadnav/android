package com.truethat.android.welcome;

import android.content.Intent;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.BaseApplicationTest;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static com.truethat.android.application.ApplicationTestUtil.withBackgroundColor;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 16/06/2017 for TrueThat.
 */
public class OnBoardingActivityTest extends BaseApplicationTest {
  private static final String NAME = "donald duck";

  @Before public void setUp() throws Exception {
    super.setUp();
    mMockAuthModule.setOnBoarded(false);
    // Authentication should navigate user to on boarding. We start from test activity,
    // so that we can assert successful on boarding.
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    onView(isRoot()).perform(waitMatcher(allOf(isDisplayed(), withId(R.id.onBoardingActivity)),
        TimeUnit.SECONDS.toMillis(1)));
  }

  @Test public void successfulOnBoarding() throws Exception {
    onView(withId(R.id.nameEditText)).perform(typeText(NAME)).perform(pressImeActionButton());
    // Wait until detection had started.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mMockReactionDetectionModule.isDetecting();
      }
    });
    // Detect smile.
    mMockReactionDetectionModule.doDetection(OnBoardingActivity.REACTION_FOR_DONE);
    assertOnBoardingSuccessful();
  }

  @Test public void alreadyAuthOk() throws Exception {
    // Do on boarding.
    onView(withId(R.id.nameEditText)).perform(typeText(NAME)).perform(pressImeActionButton());
    // Wait until detection had started.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mMockReactionDetectionModule.isDetecting();
      }
    });
    // Detect smile.
    mMockReactionDetectionModule.doDetection(OnBoardingActivity.REACTION_FOR_DONE);
    assertOnBoardingSuccessful();
    // Go to on boarding by mistake.
    mActivityTestRule.getActivity()
        .startActivity(new Intent(mActivityTestRule.getActivity(), OnBoardingActivity.class));
    // Should navigate back to test activity.
    onView(isRoot()).perform(
        waitMatcher(allOf(isDisplayed(), withId(R.id.testActivity)), TimeUnit.SECONDS.toMillis(1)));
  }

  @Test public void slowDetection() throws Exception {
    onView(withId(R.id.nameEditText)).perform(typeText(NAME)).perform(pressImeActionButton());
    // Request first input.
    mMockReactionDetectionModule.next();
    // Request second input.
    mMockReactionDetectionModule.next();
    // Slow detection... should show encouragement text
    onView(isRoot()).perform(
        waitMatcher(allOf(isDisplayed(), withId(R.id.realLifeText)), TimeUnit.SECONDS.toMillis(1)));
    mMockReactionDetectionModule.doDetection(OnBoardingActivity.REACTION_FOR_DONE);
    assertOnBoardingSuccessful();
  }

  @Test public void typingName() throws Exception {
    onView(withId(R.id.nameEditText)).perform(typeText(NAME.split(" ")[0]));
    assertInvalidName();
    onView(withId(R.id.nameEditText)).perform(typeText(" " + NAME.split(" ")[1]));
    assertValidName();
    onView(withId(R.id.nameEditText)).perform(pressImeActionButton());
    assertReadyForSmile();
    // Detect smile.
    mMockReactionDetectionModule.doDetection(OnBoardingActivity.REACTION_FOR_DONE);
    assertOnBoardingSuccessful();
  }

  private void assertValidName() {
    onView(withId(R.id.nameEditText)).check(matches(withBackgroundColor(
        mActivityTestRule.getActivity()
            .getResources()
            .getColor(OnBoardingActivity.VALID_NAME_COLOR,
                mActivityTestRule.getActivity().getTheme()))));
  }

  private void assertReadyForSmile() {
    // Wait until smile text is shown
    onView(isRoot()).perform(
        waitMatcher(allOf(isDisplayed(), withId(R.id.smileText)), TimeUnit.SECONDS.toMillis(1)));
    // Assert detection is ongoing.
    assertTrue(mMockReactionDetectionModule.isDetecting());
  }

  private void assertInvalidName() {
    // Error color indicator is shown.
    onView(withId(R.id.nameEditText)).check(matches(withBackgroundColor(
        mActivityTestRule.getActivity()
            .getResources()
            .getColor(OnBoardingActivity.ERROR_COLOR,
                mActivityTestRule.getActivity().getTheme()))));
    try {
      Thread.sleep(100);
    } catch (Exception e) {
      assertTrue(false);
    }
    // Detection is NOT ongoing.
    assertFalse(mMockReactionDetectionModule.isDetecting());
    // Smile text is hidden
    onView(withId(R.id.smileText)).check(matches(not(isDisplayed())));
  }

  private void assertOnBoardingSuccessful() {
    // Should navigate back to Test activity.
    onView(isRoot()).perform(
        waitMatcher(allOf(isDisplayed(), withId(R.id.testActivity)), TimeUnit.SECONDS.toMillis(1)));
    // Assert the current user now the proper name.
    assertEquals(NAME, App.getAuthModule().getUser().getDisplayName());
    // Assert the Auth OK status is correct.
    assertTrue(App.getAuthModule().isAuthOk());
  }
}