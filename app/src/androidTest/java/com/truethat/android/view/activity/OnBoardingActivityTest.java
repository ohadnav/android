package com.truethat.android.view.activity;

import android.content.Intent;
import android.widget.EditText;
import com.truethat.android.R;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.model.User;
import com.truethat.android.viewmodel.OnBoardingViewModel;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
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
import static android.support.test.espresso.matcher.ViewMatchers.withText;
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
public class OnBoardingActivityTest extends BaseApplicationTestSuite {
  private static final String NAME = "Matt Damon";
  private OnBoardingViewModel mViewModel;

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

  @Test public void onBoardingFlow() throws Exception {
    mFakeAuthManager.useNetwork();
    onView(withId(R.id.nameEditText)).check(matches(hasFocus()));
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
    mFakeReactionDetectionManager.doDetection(OnBoardingViewModel.REACTION_FOR_DONE);
    // Set up server response
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        User user = NetworkUtil.GSON.fromJson(request.getBody().readUtf8(), User.class);
        assertEquals(NAME, user.getDisplayName());
        user.setId(1L);
        MockResponse response = new MockResponse().setBody(NetworkUtil.GSON.toJson(user));
        response.throttleBody(20, 100, TimeUnit.MILLISECONDS);
        return response;
      }
    });
    // Loading image should be visible
    waitMatcher(allOf(withId(R.id.loadingImage), isDisplayed()));
    // On Boarding should be completed.
    assertOnBoardingSuccessful();
  }

  @Test public void alreadyAuthOk() throws Exception {
    doOnBoarding();
    assertOnBoardingSuccessful();
    // Go to on boarding by mistake.
    mActivityTestRule.getActivity()
        .startActivity(new Intent(mActivityTestRule.getActivity(), OnBoardingActivity.class));
    // Should navigate back to theater activity.
    waitForActivity(TheaterActivity.class);
  }

  @Test public void failedOnBoarding() throws Exception {
    mFakeAuthManager.useNetwork();
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        User user = NetworkUtil.GSON.fromJson(request.getBody().readUtf8(), User.class);
        assertEquals(NAME, user.getDisplayName());
        MockResponse response =
            new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
        Thread.sleep(500);
        return response;
      }
    });
    doOnBoarding();
    assertFalse(mFakeAuthManager.isAuthOk());
    // Warning text is changed
    waitMatcher(allOf(withId(R.id.warningText), isDisplayed(),
        withText(R.string.sign_up_failed_warning_text)));
    // Loading is hidden
    waitMatcher(allOf(withId(R.id.loadingImage), not(isDisplayed())));
  }

  /**
   * Programmatically completes the on boarding process as if a user completed it.
   *
   */
  private void doOnBoarding() {
    // Should navigate to On-Boarding
    waitForActivity(OnBoardingActivity.class);
    // Type user name and hit done.
    onView(withId(R.id.nameEditText)).perform(typeText(OnBoardingActivityTest.NAME))
        .perform(pressImeActionButton());
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(OnBoardingViewModel.Stage.FINAL, mViewModel.getStage());
      }
    });
    // Wait until detection had started.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mFakeReactionDetectionManager.isSubscribed(mViewModel);
      }
    });
    // Detect smile.
    mFakeReactionDetectionManager.doDetection(OnBoardingViewModel.REACTION_FOR_DONE);
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(OnBoardingViewModel.Stage.REQUEST_SENT, mViewModel.getStage());
      }
    });

  }

  private void assertReadyForSmile() {
    // Wait until smile text is shown.
    waitMatcher(allOf(isDisplayed(), withId(R.id.finalStageText)));
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

  private void assertValidName() {
    onView(withId(R.id.nameEditText)).check(matches(withBackgroundColor(
        mActivityTestRule.getActivity()
            .getResources()
            .getColor(OnBoardingViewModel.VALID_NAME_COLOR,
                mActivityTestRule.getActivity().getTheme()))));
  }

  private void assertInvalidName() {
    // Error color indicator is shown.
    onView(withId(R.id.nameEditText)).check(matches(withBackgroundColor(
        mActivityTestRule.getActivity()
            .getResources()
            .getColor(OnBoardingViewModel.ERROR_COLOR,
                mActivityTestRule.getActivity().getTheme()))));
    try {
      Thread.sleep(100);
    } catch (Exception e) {
      assertTrue(false);
    }
    // Detection is NOT ongoing.
    assertFalse(mFakeReactionDetectionManager.isSubscribed(mViewModel));
    // Smile text is hidden
    onView(withId(R.id.finalStageText)).check(matches(not(isDisplayed())));
  }

  private void assertOnBoardingSuccessful() {
    // Should navigate back to Test activity.
    waitForActivity(TheaterActivity.class);
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