package com.truethat.android.ui.activity;

import android.content.Intent;
import android.widget.EditText;
import com.truethat.android.R;
import com.truethat.android.application.auth.FakeAuthManager;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.empathy.FakeReactionDetectionManager;
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
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.getCurrentActivity;
import static com.truethat.android.application.ApplicationTestUtil.isKeyboardVisible;
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
  private static final String NAME = "Matt Damon";

  /**
   * Programmatically completes the on boarding process as if a user completed it.
   *  @param name                        of the new user.
   * @param reactionDetectionManager to detect {@link OnBoardingActivity#REACTION_FOR_DONE}.
   * @param fakeAuthManager to perform the on boarding with.
   */
  private static void doOnBoarding(String name,
      final FakeReactionDetectionManager reactionDetectionManager,
      final FakeAuthManager fakeAuthManager) {
    // Should navigate to On-Boarding
    waitForActivity(OnBoardingActivity.class);
    // Type user name and hit done.
    onView(withId(R.id.nameEditText)).perform(typeText(name)).perform(pressImeActionButton());
    // Wait until detection had started.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return reactionDetectionManager.isDetecting();
      }
    });
    // Detect smile.
    reactionDetectionManager.doDetection(OnBoardingActivity.REACTION_FOR_DONE);
    // Wait until Auth OK.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(fakeAuthManager.isAuthOk());
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
  }

  @Test public void successfulOnBoarding() throws Exception {
    // EditText should be auto focused.
    onView(withId(R.id.nameEditText)).check(matches(hasFocus()));
    doOnBoarding(NAME, mFakeReactionDetectionManager, mFakeAuthManager);
    assertOnBoardingSuccessful();
  }

  @Test public void alreadyAuthOk() throws Exception {
    doOnBoarding(NAME, mFakeReactionDetectionManager, mFakeAuthManager);
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
    mFakeReactionDetectionManager.next();
    // Request second input.
    mFakeReactionDetectionManager.next();
    // Slow detection... should show encouragement text
    waitMatcher(allOf(isDisplayed(), withId(R.id.realLifeText)));
    // Compete on boarding
    mFakeReactionDetectionManager.doDetection(OnBoardingActivity.REACTION_FOR_DONE);
    assertOnBoardingSuccessful();
  }

  @Test public void typingName() throws Exception {
    final EditText editText = (EditText) getCurrentActivity().findViewById(R.id.nameEditText);
    // Type first name
    onView(withId(R.id.nameEditText)).perform(typeText(NAME.split(" ")[0]));
    // Cursor should be visible.
    assertTrue(editText.isCursorVisible());
    assertInvalidName();
    // Lose focus, keyboard and cursor should be hidden.
    getCurrentActivity().runOnUiThread(new Runnable() {
      @Override public void run() {
        editText.clearFocus();
      }
    });
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertFalse(isKeyboardVisible());
      }
    });
    assertFalse(editText.isCursorVisible());
    // Type again, and assert keyboard and cursor are visible again.
    onView(withId(R.id.nameEditText)).perform(typeText(" "));
    assertTrue(isKeyboardVisible());
    assertTrue(editText.isCursorVisible());
    // Hit done (ime button).
    onView(withId(R.id.nameEditText)).perform(pressImeActionButton());
    // Should not be moving to next stage
    assertInvalidName();
    // Cursor and keyboard should be hidden.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertFalse(isKeyboardVisible());
      }
    });
    assertFalse(editText.isCursorVisible());
    // Warning text visible.
    waitMatcher(allOf(withId(R.id.warningText), isDisplayed()));
    // Type last name
    onView(withId(R.id.nameEditText)).perform(typeText(NAME.split(" ")[1]));
    assertValidName();
    onView(withId(R.id.nameEditText)).perform(pressImeActionButton());
    assertReadyForSmile();
    // Cursor should be hidden after hitting ime button.
    assertFalse(editText.isCursorVisible());
    // Detect smile.
    mFakeReactionDetectionManager.doDetection(OnBoardingActivity.REACTION_FOR_DONE);
    assertOnBoardingSuccessful();
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
    assertFalse(mFakeReactionDetectionManager.isDetecting());
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
    // Wait until smile text is shown.
    waitMatcher(allOf(isDisplayed(), withId(R.id.smileText)));
    // Warning text should be hidden.
    onView(withId(R.id.warningText)).check(matches(not(isDisplayed())));
    // Assert detection is ongoing.
    assertTrue(mFakeReactionDetectionManager.isDetecting());
    // Keyboard should be hidden.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertFalse(isKeyboardVisible());
      }
    });
  }
}