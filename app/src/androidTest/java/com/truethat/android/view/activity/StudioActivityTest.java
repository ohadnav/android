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
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Scene;
import com.truethat.android.view.fragment.CameraFragment;
import com.truethat.android.view.fragment.MediaFragment;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Before;
import org.junit.Ignore;
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
import static com.truethat.android.view.activity.StudioActivity.DIRECTED_SCENE_TAG;
import static com.truethat.android.viewmodel.StudioViewModel.DirectingState.SENT;
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
    assertCameraState();
    setDispatcher(new CountingDispatcher() {
      @Override public MockResponse processRequest(RecordedRequest request) throws Exception {
        Thread.sleep(BaseApplicationTestSuite.TIMEOUT.getValueInMS() / 2);
        Scene directed = mStudioActivityTestRule.getActivity().getViewModel().getDirectedScene();
        directed.setId(1L);
        return new MockResponse().setBody(GSON.toJson(directed));
      }
    });
  }

  @Test public void sendPhotoFlow() throws Exception {
    // Take a picture
    onView(withId(R.id.captureButton)).perform(click());
    // Should proceed to edit state
    assertEditState();
    onView(withId(R.id.sendButton)).perform(click());
    // Should proceed to sent state
    assertSentState();
    // Should proceed to published state
    assertPublishedState();
  }

  @Test public void sendInteractiveFlow() throws Exception {
    // Take a picture
    onView(withId(R.id.captureButton)).perform(click());
    // Should proceed to edit state
    assertEditState();
    // Chose a reaction
    onView(withId(mStudioActivityTestRule.getActivity().getEmotionToViewId().get(Emotion.SURPRISE)))
        .perform(click());
    // Should proceed to camera state.
    assertCameraState();
    // Record a video
    onView(withId(R.id.captureButton)).perform(
        new GeneralClickAction(new RecordTapper(3000), GeneralLocation.CENTER, Press.FINGER));
    // Should proceed to edit state
    assertEditState();
    // Go back to root media to create an alternative
    onView(withId(R.id.previousMedia)).perform(click());
    assertEditState();
    onView(withId(
        mStudioActivityTestRule.getActivity().getEmotionToViewId().get(Emotion.HAPPY))).perform(
        click());
    // Should proceed to camera state.
    assertCameraState();
    // Take a picture
    onView(withId(R.id.captureButton)).perform(click());
    // Should proceed to edit state
    assertEditState();
    // Redo the surprise ending
    onView(withId(R.id.previousMedia)).perform(click());
    assertEditState();
    onView(withId(mStudioActivityTestRule.getActivity().getEmotionToViewId().get(Emotion.SURPRISE)))
        .perform(click());
    assertEditState();
    onView(withId(R.id.cancelButton)).perform(click());
    assertEditState();
    onView(withId(mStudioActivityTestRule.getActivity().getEmotionToViewId().get(Emotion.SURPRISE)))
        .perform(click());
    // Should proceed to camera state.
    assertCameraState();
    // Record a video
    onView(withId(R.id.captureButton)).perform(
        new GeneralClickAction(new RecordTapper(3000), GeneralLocation.CENTER, Press.FINGER));
    // Should proceed to edit state
    assertEditState();
    // Send the scene
    onView(withId(R.id.sendButton)).perform(click());
    // Should proceed to sent state
    assertSentState();
    // Should proceed to published state
    assertPublishedState();
  }

  @Test public void sendVideoFlow() throws Exception {
    // Record a video
    onView(withId(R.id.captureButton)).perform(
        new GeneralClickAction(new RecordTapper(), GeneralLocation.CENTER, Press.FINGER));
    // Should proceed to edit state
    assertEditState();
    onView(withId(R.id.sendButton)).perform(click());
    // Should proceed to sent state
    assertSentState();
    // Should proceed to published state
    assertPublishedState();
  }

  @Test public void takePictureWithButton() throws Exception {
    // Take a picture
    onView(withId(R.id.captureButton)).perform(click());
    // Should proceed to edit state
    assertEditState();
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(((MediaFragment) mStudioActivityTestRule.getActivity()
            .getSupportFragmentManager()
            .findFragmentByTag(DIRECTED_SCENE_TAG)).isReady());
      }
    });
    onView(withId(R.id.imageView)).check(matches(isFullScreen()));
  }

  @Test public void takePhotoAndResumeDirecting() throws Exception {
    // Start recording.
    onView(withId(R.id.captureButton)).perform(click());
    // Should proceed to edit state
    assertEditState();
    // Resume to directing state
    onView(withId(R.id.cancelButton)).perform(click());
    assertCameraState();
  }

  @Test public void recordVideoWithButton() throws Exception {
    // Start recording.
    onView(withId(R.id.captureButton)).perform(
        new GeneralClickAction(new RecordTapper(), GeneralLocation.CENTER, Press.FINGER));
    // Should proceed to edit state
    assertEditState();
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(((MediaFragment) mStudioActivityTestRule.getActivity()
            .getSupportFragmentManager()
            .findFragmentByTag(DIRECTED_SCENE_TAG)).isReady());
      }
    });
    onView(withId(R.id.videoSurface)).check(matches(isFullScreen()));
  }

  @Test public void recordVideoAndResumeDirecting() throws Exception {
    // Start recording.
    onView(withId(R.id.captureButton)).perform(
        new GeneralClickAction(new RecordTapper(), GeneralLocation.CENTER, Press.FINGER));
    // Should proceed to edit state
    assertEditState();
    // Resume to directing state
    onView(withId(R.id.cancelButton)).perform(click());
    assertCameraState();
  }

  // Test takes forever to complete, so ignore it.
  @Ignore @Test public void notTakingPictureWhenNotAuth() throws Exception {
    mFakeAuthManager.forbidAuth();
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
    assertEditState();
    // Navigate out of studio
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeDown());
    waitForActivity(TheaterActivity.class);
    // Navigate back to studio
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeUp());
    waitForActivity(StudioActivity.class);
    // Should remain in edit state
    assertEditState();
  }

  @Test public void cameraState() throws Exception {
    assertCameraState();
  }

  @Test public void switchCamera() throws Exception {
    // Switch front with back camera
    onView(withId(R.id.switchCameraButton)).perform(click());
    assertCameraState();
  }

  private void assertCameraState() {
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
    // Directed scene preview is hidden
    onView(withId(R.id.previewContainer)).check(matches(not(isDisplayed())));
  }

  private void assertEditState() {
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
    // Directed scene preview is shown
    onView(withId(R.id.previewContainer)).check(matches(isDisplayed()));
    final MediaFragment mediaFragment = (MediaFragment) mStudioActivityTestRule.getActivity()
        .getSupportFragmentManager()
        .findFragmentByTag(DIRECTED_SCENE_TAG);
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        //noinspection unchecked
        assertTrue(mediaFragment.isReady());
      }
    });
    assertEquals(mStudioActivityTestRule.getActivity().getViewModel().getCurrentMedia(),
        mediaFragment.getMedia());
    // Should display previous media if not editing root media
    if (mediaFragment.getMedia()
        .equals(mStudioActivityTestRule.getActivity()
            .getViewModel().getDirectedScene().getRootMedia())) {
      onView(withId(R.id.previousMedia)).check(matches(not(isDisplayed())));
    } else {
      onView(withId(R.id.previousMedia)).check(matches(isDisplayed()));
    }
    // Should display emotions to create interactive scenes from.
    for (Emotion emotion : Emotion.values()) {
      onView(withId(mStudioActivityTestRule.getActivity().getEmotionToViewId().get(emotion))).check(
          matches(isDisplayed()));
    }
  }

  private void assertSentState() {
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(SENT, mStudioActivityTestRule.getActivity().getViewModel().getState());
      }
    });
    // Wait until camera preview is frozen.
    assertNotEquals(CameraFragment.CameraState.PREVIEW, mCameraFragment.getState());
    // Buttons are hidden
    waitMatcher(allOf(withId(R.id.sendButton), not(isDisplayed())));
    onView(withId(R.id.cancelButton)).check(matches(not(isDisplayed())));
    onView(withId(R.id.captureButton)).check(matches(not(isDisplayed())));
    onView(withId(R.id.switchCameraButton)).check(matches(not(isDisplayed())));
    // Loading image is show
    assertEquals(VISIBLE, mStudioActivityTestRule.getActivity().mLoadingImage.getVisibility());
    // Directed scene preview is shown
    onView(withId(R.id.previewContainer)).check(matches(isDisplayed()));
    // Network request had been made
    assertEquals(1, mDispatcher.getCount(StudioApi.PATH));
  }

  private void assertPublishedState() {
    waitForActivity(TheaterActivity.class, TIMEOUT.plus(TIMEOUT));
    // Should display toast
    onView(withText(R.string.saved_successfully)).inRoot(
        withDecorView(not(mStudioActivityTestRule.getActivity().getWindow().getDecorView())))
        .check(matches(isDisplayed()));
  }

  @SuppressWarnings({ "ResultOfMethodCallIgnored", "SameParameterValue" })
  private class RecordTapper implements Tapper {
    private int videoLength;

    RecordTapper(int videoLength) {
      this.videoLength = videoLength;
    }

    RecordTapper() {
      videoLength = 2000;
    }

    @Override
    public Status sendTap(UiController uiController, float[] coordinates, float[] precision) {
      checkNotNull(uiController);
      checkNotNull(coordinates);
      checkNotNull(precision);

      MotionEvent downEvent = MotionEvents.sendDown(uiController, coordinates, precision).down;
      try {
        uiController.loopMainThreadForAtLeast(videoLength);

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