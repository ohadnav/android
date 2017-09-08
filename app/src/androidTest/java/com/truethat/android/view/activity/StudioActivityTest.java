package com.truethat.android.view.activity;

import android.support.test.espresso.UiController;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.MotionEvents;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tapper;
import android.support.test.espresso.action.ViewActions;
import android.support.test.filters.FlakyTest;
import android.support.test.rule.ActivityTestRule;
import android.view.MotionEvent;
import com.truethat.android.R;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.common.network.StudioApi;
import com.truethat.android.common.util.CountingDispatcher;
import com.truethat.android.model.Reactable;
import com.truethat.android.view.fragment.CameraFragment;
import com.truethat.android.view.fragment.MediaFragment;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.truethat.android.application.ApplicationTestUtil.centerSwipeUp;
import static com.truethat.android.application.ApplicationTestUtil.isFullScreen;
import static com.truethat.android.application.ApplicationTestUtil.waitForActivity;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static com.truethat.android.common.network.NetworkUtil.GSON;
import static com.truethat.android.view.activity.StudioActivity.DIRECTED_REACTABLE_TAG;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */
public class StudioActivityTest extends BaseApplicationTestSuite {
  @Rule public ActivityTestRule<StudioActivity> mStudioActivityTestRule =
      new ActivityTestRule<>(StudioActivity.class, true, false);
  private CameraFragment mCameraFragment;

  @Before public void setUp() throws Exception {
    super.setUp();
    // Launches studio activity.
    mStudioActivityTestRule.launchActivity(null);
    mCameraFragment = (CameraFragment) mStudioActivityTestRule.getActivity()
        .getSupportFragmentManager()
        .findFragmentById(R.id.cameraFragment);
    assertDirectingState();
    setDispatcher(new CountingDispatcher() {
      @Override public MockResponse processRequest(RecordedRequest request) throws Exception {
        Thread.sleep(BaseApplicationTestSuite.TIMEOUT.getValueInMS() / 2);
        Reactable directed =
            mStudioActivityTestRule.getActivity().getViewModel().getDirectedReactable();
        directed.setId(1L);
        return new MockResponse().setBody(GSON.toJson(directed));
      }
    });
  }

  @Test public void sendPoseFlow() throws Exception {
    // Take a picture
    onView(withId(R.id.captureButton)).perform(click());
    // Should proceed to approval state
    assertApprovalState();
    onView(withId(R.id.sendButton)).perform(click());
    // Should proceed to sent state
    assertSentState();
    // Should proceed to published state
    assertPublishedState();
  }

  @Test public void sendShortFlow() throws Exception {
    // Take a picture
    onView(withId(R.id.captureButton)).perform(
        new GeneralClickAction(new RecordTapper(), GeneralLocation.CENTER, Press.FINGER));
    // Should proceed to approval state
    assertApprovalState();
    onView(withId(R.id.sendButton)).perform(click());
    // Should proceed to sent state
    assertSentState();
    // Should proceed to published state
    assertPublishedState();
  }

  @Test public void takePictureWithButton() throws Exception {
    // Take a picture
    onView(withId(R.id.captureButton)).perform(click());
    // Should proceed to approval state
    assertApprovalState();
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(((MediaFragment) mStudioActivityTestRule.getActivity()
            .getSupportFragmentManager()
            .findFragmentByTag(DIRECTED_REACTABLE_TAG)).isReady());
      }
    });
    onView(withId(R.id.imageView)).check(matches(isFullScreen()));
  }

  @Test public void takePhotoAndResumeDirecting() throws Exception {
    // Start recording.
    onView(withId(R.id.captureButton)).perform(click());
    // Should proceed to approval state
    assertApprovalState();
    // Resume to directing state
    onView(withId(R.id.cancelButton)).perform(click());
    assertDirectingState();
  }

  @Test public void recordVideoWithButton() throws Exception {
    // Start recording.
    onView(withId(R.id.captureButton)).perform(
        new GeneralClickAction(new RecordTapper(), GeneralLocation.CENTER, Press.FINGER));
    // Should proceed to approval state
    assertApprovalState();
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(((MediaFragment) mStudioActivityTestRule.getActivity()
            .getSupportFragmentManager()
            .findFragmentByTag(DIRECTED_REACTABLE_TAG)).isReady());
      }
    });
    onView(withId(R.id.videoSurface)).check(matches(isFullScreen()));
  }

  @Test public void recordVideoAndResumeDirecting() throws Exception {
    // Start recording.
    onView(withId(R.id.captureButton)).perform(
        new GeneralClickAction(new RecordTapper(), GeneralLocation.CENTER, Press.FINGER));
    // Should proceed to approval state
    assertApprovalState();
    // Resume to directing state
    onView(withId(R.id.cancelButton)).perform(click());
    assertDirectingState();
  }

  @Test public void notTakingPictureWhenNotAuth() throws Exception {
    mFakeAuthManager.setAllowAuth(false);
    onView(withId(R.id.captureButton)).perform(click());
    // Ensuring signing in Toast is shown.
    onView(withText(R.string.signing_in)).inRoot(
        withDecorView(not(mStudioActivityTestRule.getActivity().getWindow().getDecorView())))
        .check(matches(isDisplayed()));
    // Should navigate to welcome activity
    waitForActivity(WelcomeActivity.class);
  }

  @Test public void navigationToTheater() throws Exception {
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeDown());
    waitForActivity(TheaterActivity.class);
  }

  @Test public void navigationToRepertoire() throws Exception {
    // Regular swipe up interferes with the capture button, and so the test fails.
    centerSwipeUp();
    waitForActivity(RepertoireActivity.class);
  }

  @Test @FlakyTest public void singleInstance() throws Exception {
    // Take a picture
    onView(withId(R.id.captureButton)).perform(click());
    assertApprovalState();
    // Navigate out of studio
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeDown());
    waitForActivity(TheaterActivity.class);
    // Navigate back to studio
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeUp());
    waitForActivity(StudioActivity.class);
    // Should remain in approval state
    assertApprovalState();
  }

  @Test public void directingState() throws Exception {
    assertDirectingState();
  }

  @Test public void switchCamera() throws Exception {
    // Switch front with back camera
    onView(withId(R.id.switchCameraButton)).perform(click());
    assertDirectingState();
  }

  private void assertDirectingState() {
    // Wait until camera preview is live.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(CameraFragment.CameraState.PREVIEW, mCameraFragment.getState());
      }
    });
    // Capture buttons are displayed.
    waitMatcher(allOf(withId(R.id.captureButton), isDisplayed()));
    onView(withId(R.id.switchCameraButton)).check(matches(isDisplayed()));
    // Approval buttons are hidden
    onView(withId(R.id.sendButton)).check(matches(not(isDisplayed())));
    onView(withId(R.id.cancelButton)).check(matches(not(isDisplayed())));
    // Loading image is hidden
    assertEquals(GONE, mStudioActivityTestRule.getActivity().mLoadingImage.getVisibility());
    // Directed reactable preview is hidden
    onView(withId(R.id.previewContainer)).check(matches(not(isDisplayed())));
  }

  private void assertApprovalState() {
    // Wait until camera preview is frozen.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertNotEquals(CameraFragment.CameraState.PREVIEW, mCameraFragment.getState());
      }
    });
    // Approval buttons are shown
    waitMatcher(allOf(withId(R.id.sendButton), isDisplayed()));
    onView(withId(R.id.cancelButton)).check(matches(isDisplayed()));
    // Capture buttons are hidden.
    onView(withId(R.id.captureButton)).check(matches(not(isDisplayed())));
    onView(withId(R.id.switchCameraButton)).check(matches(not(isDisplayed())));
    // Loading image is hidden
    assertEquals(GONE, mStudioActivityTestRule.getActivity().mLoadingImage.getVisibility());
    // Directed reactable preview is shown
    onView(withId(R.id.previewContainer)).check(matches(isDisplayed()));
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        //noinspection unchecked
        assertTrue(((MediaFragment) mStudioActivityTestRule
                .getActivity()
            .getSupportFragmentManager()
            .findFragmentByTag(DIRECTED_REACTABLE_TAG)).isReady());
      }
    });
  }

  private void assertSentState() {
    // Wait until camera preview is frozen.
    assertNotEquals(CameraFragment.CameraState.PREVIEW, mCameraFragment.getState());
    // Buttons are hidden
    waitMatcher(allOf(withId(R.id.sendButton), not(isDisplayed())));
    onView(withId(R.id.cancelButton)).check(matches(not(isDisplayed())));
    onView(withId(R.id.captureButton)).check(matches(not(isDisplayed())));
    onView(withId(R.id.switchCameraButton)).check(matches(not(isDisplayed())));
    // Loading image is show
    assertEquals(VISIBLE, mStudioActivityTestRule.getActivity().mLoadingImage.getVisibility());
    // Directed reactable preview is shown
    onView(withId(R.id.previewContainer)).check(matches(isDisplayed()));
    // Network request had been made
    assertEquals(1, mDispatcher.getCount(StudioApi.PATH));
  }

  private void assertPublishedState() {
    waitForActivity(TheaterActivity.class);
    // Should display toast
    onView(withText(R.string.saved_successfully)).inRoot(
        withDecorView(not(mStudioActivityTestRule.getActivity().getWindow().getDecorView())))
        .check(matches(isDisplayed()));
  }

  @SuppressWarnings("ResultOfMethodCallIgnored") private class RecordTapper implements Tapper {
    @Override
    public Status sendTap(UiController uiController, float[] coordinates, float[] precision) {
      checkNotNull(uiController);
      checkNotNull(coordinates);
      checkNotNull(precision);

      MotionEvent downEvent = MotionEvents.sendDown(uiController, coordinates, precision).down;
      try {
        uiController.loopMainThreadForAtLeast(2000);

        if (!MotionEvents.sendUp(uiController, downEvent)) {
          MotionEvents.sendCancel(uiController, downEvent);
          return Tapper.Status.FAILURE;
        }
      } finally {
        downEvent.recycle();
        //noinspection UnusedAssignment
        downEvent = null;
      }
      return Tapper.Status.SUCCESS;
    }
  }
}