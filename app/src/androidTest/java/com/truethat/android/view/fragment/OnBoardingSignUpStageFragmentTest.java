package com.truethat.android.view.fragment;

import android.widget.EditText;
import com.truethat.android.R;
import com.truethat.android.common.BaseInstrumentationTestSuite;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.model.User;
import com.truethat.android.view.activity.BaseOnBoardingTest;
import com.truethat.android.view.activity.OnBoardingActivity;
import com.truethat.android.viewmodel.OnBoardingCheeseStageViewModel;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.truethat.android.application.ApplicationTestUtil.getCurrentActivity;
import static com.truethat.android.application.ApplicationTestUtil.isKeyboardVisible;
import static com.truethat.android.application.ApplicationTestUtil.waitForBaseDialog;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 25/10/2017 for TrueThat.
 */
public class OnBoardingSignUpStageFragmentTest extends BaseOnBoardingTest {

  @Test public void typing() throws Exception {
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
    manualSetUp();
    mFakeAuthManager.useNetwork();
    final EditText editText = (EditText) getCurrentActivity().findViewById(R.id.nameEditText);
    onView(withId(R.id.nameEditText)).check(matches(hasFocus()));
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(isKeyboardVisible());
        assertTrue(editText.isCursorVisible());
      }
    });
    // For some reason typing fails initially.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        // Type first name
        onView(withId(R.id.nameEditText)).perform(typeText(NAME.split(" ")[0]));
      }
    });
    // Cursor should be visible.
    assertTrue(editText.isCursorVisible());
    assertSigningUpInvalidName();
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
    assertSigningUpInvalidName();
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
    assertSigningUpValidName();
    onView(withId(R.id.nameEditText)).perform(pressImeActionButton());
    // Loading image should be visible
    waitMatcher(allOf(withId(R.id.onBoarding_signUp_loadingImage), isDisplayed()));
    // On Boarding should be completed.
    assertOnBoardingSuccessful();
  }

  @Test public void signUpWithButton() throws Exception {
    manualSetUp();
    // Type user name.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        onView(withId(R.id.nameEditText)).perform(typeText(BaseOnBoardingTest.NAME));
      }
    });
    // Click on main layout to remove name edit focus
    onView(withId(android.R.id.content)).perform(click());
    // Wait for keyboard to be hidden
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertFalse(isKeyboardVisible());
      }
    });
    // Click sign up button
    onView(withId(R.id.onBoarding_signUpButton)).perform(click());
    assertOnBoardingSuccessful();
  }

  @Test public void failedRequest() throws Exception {
    doSignOut();
    mFakeAuthManager.useNetwork();
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        User user = NetworkUtil.GSON.fromJson(request.getBody().readUtf8(), User.class);
        assertEquals(NAME, user.getDisplayName());
        MockResponse response =
            new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
        Thread.sleep(Math.min(BaseInstrumentationTestSuite.TIMEOUT.getValueInMS() / 2, 500));
        return response;
      }
    });
    doOnBoarding();
    assertFalse(mFakeAuthManager.isAuthOk());
    // Loading is hidden
    waitMatcher(allOf(withId(R.id.onBoarding_signUp_loadingImage), not(isDisplayed())));
    // Dialog is visible
    waitForBaseDialog();
  }

  @Test public void testStageEditSaved() throws Exception {
    manualSetUp();
    // For some reason typing fails initially.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        // Type user name
        onView(withId(R.id.nameEditText)).perform(typeText(NAME));
      }
    });
    // Destroy activity and resume to it.
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override public void run() {
        mActivity.recreate();
      }
    });
    // Detection should not start
    Thread.sleep(BaseInstrumentationTestSuite.TIMEOUT.getValueInMS() / 2);
    assertFalse(mFakeReactionDetectionManager.isDetecting());
    waitMatcher(allOf(withId(R.id.nameEditText), isDisplayed(), withText(NAME)));
  }

  @Test public void testStageRequestSentSaved() throws Exception {
    mFakeAuthManager.useNetwork();
    final int[] requestCounter = new int[1];
    requestCounter[0] = 0;
    final Dispatcher dispatcher = new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        requestCounter[0] = requestCounter[0] + 1;
        Thread.sleep(BaseInstrumentationTestSuite.TIMEOUT.getValueInMS() / 2);
        User user = NetworkUtil.GSON.fromJson(request.getBody().readUtf8(), User.class);
        user.setId(1L);
        return new MockResponse().setBody(NetworkUtil.GSON.toJson(user));
      }
    };
    // Set up backend delay
    mMockWebServer.setDispatcher(dispatcher);
    manualSetUp();
    // For some reason typing fails initially.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        // Type user name
        onView(withId(R.id.nameEditText)).perform(typeText(NAME));
      }
    });
    // Hit done (ime button).
    onView(withId(R.id.nameEditText)).perform(pressImeActionButton());
    // Should have sent a request to the backend.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(1, requestCounter[0]);
      }
    });
    // Destroy activity and resume to it.
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override public void run() {
        mActivity.recreate();
      }
    });
    // Should resume to request sent stage and send a request to the backend.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(2, requestCounter[0]);
      }
    });
  }

  @Override protected void manualSetUp() {
    super.manualSetUp();
    // Proceed to cheese stage
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override public void run() {
        //noinspection ConstantConditions
        mActivity.mHiStageFragment.getView().findViewById(R.id.onBoarding_hiButton).performClick();
      }
    });
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
  }
}