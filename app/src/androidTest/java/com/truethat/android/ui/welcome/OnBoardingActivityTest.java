package com.truethat.android.ui.welcome;

import android.content.Intent;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.ui.common.TestActivity;
import com.truethat.android.ui.common.camera.CameraFragment;
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
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.getCurrentActivity;
import static com.truethat.android.application.ApplicationTestUtil.waitForActivity;
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
public class OnBoardingActivityTest extends BaseApplicationTestSuite {
  private static final String NAME = "donald duck";

  @Before public void setUp() throws Exception {
    super.setUp();
    mMockAuthModule.setOnBoarded(false);
    // Authentication should navigate user to on boarding. We start from test activity,
    // so that we can assert successful on boarding.
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    waitForActivity(OnBoardingActivity.class);
  }

  @Test public void successfulOnBoarding() throws Exception {
    // EditText should be auto focused.
    onView(withId(R.id.nameEditText)).check(matches(hasFocus()));
    // Type user name and hit done.
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
    // Type user name and hit done.
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
    waitForActivity(TestActivity.class);
  }

  @Test public void slowDetection() throws Exception {
    // Type user name.
    onView(withId(R.id.nameEditText)).perform(typeText(NAME));
    // Wait for camera to open
    final CameraFragment cameraFragment =
        (CameraFragment) getCurrentActivity().getSupportFragmentManager()
            .findFragmentById(R.id.cameraFragment);
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return cameraFragment.isCameraOpen();
      }
    });
    // Hit done.
    onView(withId(R.id.nameEditText)).perform(pressImeActionButton());
    // Request first input.
    mMockReactionDetectionModule.next();
    // Request second input.
    mMockReactionDetectionModule.next();
    // Slow detection... should show encouragement text
    onView(isRoot()).perform(waitMatcher(allOf(isDisplayed(), withId(R.id.realLifeText))));
    // Compete on boarding
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

  private void assertOnBoardingSuccessful() {
    // Should navigate back to Test activity.
    waitForActivity(TestActivity.class);
    // Wait until Auth OK.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(App.getAuthModule().isAuthOk());
      }
    });
    // Assert the current user now the proper name.
    assertEquals(NAME, App.getAuthModule().getUser().getDisplayName());
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

  private void assertValidName() {
    onView(withId(R.id.nameEditText)).check(matches(withBackgroundColor(
        mActivityTestRule.getActivity()
            .getResources()
            .getColor(OnBoardingActivity.VALID_NAME_COLOR,
                mActivityTestRule.getActivity().getTheme()))));
  }

  private void assertReadyForSmile() {
    // Wait until smile text is shown
    onView(isRoot()).perform(waitMatcher(allOf(isDisplayed(), withId(R.id.smileText))));
    // Assert detection is ongoing.
    assertTrue(mMockReactionDetectionModule.isDetecting());
  }
}