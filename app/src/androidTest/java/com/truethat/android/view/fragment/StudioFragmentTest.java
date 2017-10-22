package com.truethat.android.view.fragment;

import android.support.test.espresso.UiController;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.MotionEvents;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tapper;
import android.support.test.espresso.action.ViewActions;
import android.view.MotionEvent;
import android.view.View;
import com.truethat.android.R;
import com.truethat.android.common.BaseInstrumentationTestSuite;
import com.truethat.android.common.network.StudioApi;
import com.truethat.android.common.util.CountingDispatcher;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Scene;
import com.truethat.android.view.activity.MainActivity;
import com.truethat.android.view.activity.WelcomeActivity;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
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
import static com.truethat.android.application.ApplicationTestUtil.getAbsoluteLeft;
import static com.truethat.android.application.ApplicationTestUtil.getAbsoluteTop;
import static com.truethat.android.application.ApplicationTestUtil.getCurrentActivity;
import static com.truethat.android.application.ApplicationTestUtil.isFullScreen;
import static com.truethat.android.application.ApplicationTestUtil.waitForActivity;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static com.truethat.android.common.network.NetworkUtil.GSON;
import static com.truethat.android.viewmodel.StudioViewModel.DirectingState.SENT;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */
public class StudioFragmentTest extends BaseInstrumentationTestSuite {
  private StudioFragment mStudioFragment;
  private CameraFragment mCameraFragment;

  public static void assertEditState() {
    final MainActivity mainActivity = (MainActivity) getCurrentActivity();
    final StudioFragment studioFragment = mainActivity.getStudioFragment();
    final CameraFragment cameraFragment = (CameraFragment) studioFragment.getFragmentManager()
        .findFragmentByTag(CameraFragment.FRAGMENT_TAG);
    // Wait until camera preview is frozen.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertNotEquals(CameraFragment.CameraState.PREVIEW, cameraFragment.getState());
      }
    });
    // Approval buttons are shown
    waitMatcher(allOf(withId(R.id.sendButton), isDisplayed()));
    onView(withId(R.id.cancelButton)).check(matches(isDisplayed()));
    // Capture buttons are hidden.
    onView(withId(R.id.switchCameraButton)).check(matches(not(isDisplayed())));
    // Toolbar should be hidden
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(0f, mainActivity.mToolbarLayout.getAlpha(), 0.01);
      }
    });
    // Loading image is hidden
    assertEquals(GONE, studioFragment.mLoadingImage.getVisibility());
    // Directed scene preview is shown
    onView(withId(R.id.previewContainer)).check(matches(isDisplayed()));
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertNotNull(studioFragment.getMediaFragment());
      }
    });
    final MediaFragment mediaFragment = studioFragment.getMediaFragment();
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        //noinspection unchecked
        assertTrue(mediaFragment.isReady());
      }
    });
    assertEquals(studioFragment.getViewModel().getCurrentMedia(), mediaFragment.getMedia());
    // Should display previous media if not editing root media
    if (mediaFragment.getMedia()
        .equals(studioFragment.getViewModel().getDirectedScene().getRootMedia())) {
      onView(withId(R.id.parentMedia)).check(matches(not(isDisplayed())));
    } else {
      onView(withId(R.id.parentMedia)).check(matches(isDisplayed()));
    }
    // Should display emotions to create interactive scenes from.
    for (Emotion emotion : Emotion.values()) {
      onView(withId(studioFragment.getEmotionToViewId().get(emotion))).check(
          matches(isDisplayed()));
    }
  }

  @Before public void setUp() throws Exception {
    super.setUp();
    // Launches studio activity.
    mMainActivityRule.launchActivity(null);
    mStudioFragment = mMainActivityRule.getActivity().getStudioFragment();
    mCameraFragment = (CameraFragment) mStudioFragment.getFragmentManager()
        .findFragmentByTag(CameraFragment.FRAGMENT_TAG);
    assertCameraState();
    setDispatcher(new CountingDispatcher() {
      @Override public MockResponse processRequest(RecordedRequest request) throws Exception {
        Thread.sleep(Math.min(BaseInstrumentationTestSuite.TIMEOUT.getValueInMS() / 2, 2000));
        Scene directed = mStudioFragment.getViewModel().getDirectedScene();
        directed.setId(1L);
        return new MockResponse().setBody(GSON.toJson(directed));
      }
    });
  }

  @Test public void sendPhotoFlow() throws Exception {
    // Take a picture
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override public void run() {
        mMainActivityRule.getActivity().mToolbarStudio.performClick();
      }
    });
    // Should proceed to edit state
    assertEditState();
    onView(withId(R.id.sendButton)).perform(click());
    // Should proceed to sent state
    assertSentState(1);
    // Should proceed to published state
    assertPublishedState();
  }

  @Test public void sendInteractiveFlow() throws Exception {
    // Take a picture
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override public void run() {
        mMainActivityRule.getActivity().mToolbarStudio.performClick();
      }
    });
    // Should proceed to edit state
    assertEditState();
    // Chose a reaction
    onView(withId(mStudioFragment.getEmotionToViewId().get(Emotion.SURPRISE))).perform(click());
    // Should proceed to camera state.
    assertCameraState();
    // Record a video
    recodeVideo();
    // Should proceed to edit state
    assertEditState();
    // Go back to root media to create an alternative
    onView(withId(R.id.parentMedia)).perform(click());
    assertEditState();
    onView(withId(mStudioFragment.getEmotionToViewId().get(Emotion.HAPPY))).perform(click());
    // Should proceed to camera state.
    assertCameraState();
    // Take a picture
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override public void run() {
        mMainActivityRule.getActivity().mToolbarStudio.performClick();
      }
    });
    // Should proceed to edit state
    assertEditState();
    // Redo the surprise ending
    onView(withId(R.id.parentMedia)).perform(click());
    assertEditState();
    onView(withId(mStudioFragment.getEmotionToViewId().get(Emotion.SURPRISE))).perform(click());
    assertEditState();
    onView(withId(R.id.cancelButton)).perform(click());
    assertEditState();
    onView(withId(mStudioFragment.getEmotionToViewId().get(Emotion.SURPRISE))).perform(click());
    // Should proceed to camera state.
    assertCameraState();
    // Record a video
    recodeVideo();
    // Should proceed to edit state
    assertEditState();
    // Send the scene
    onView(withId(R.id.sendButton)).perform(click());
    // Should proceed to sent state
    assertSentState(1);
    // Should proceed to published state
    assertPublishedState();
  }

  @Test public void sendVideoFlow() throws Exception {
    // Record a video
    recodeVideo();
    // Should proceed to edit state
    assertEditState();
    onView(withId(R.id.sendButton)).perform(click());
    // Should proceed to sent state
    assertSentState(1);
    // Should proceed to published state
    assertPublishedState();
  }

  @Test public void takePictureWithButton() throws Exception {
    // Take a picture
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override public void run() {
        mMainActivityRule.getActivity().mToolbarStudio.performClick();
      }
    });
    // Should proceed to edit state
    assertEditState();
    onView(withId(R.id.imageView)).check(matches(isFullScreen()));
  }

  @Test public void takePhotoAndResumeDirecting() throws Exception {
    // Take a photo.
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override public void run() {
        mMainActivityRule.getActivity().mToolbarStudio.performClick();
      }
    });
    // Should proceed to edit state
    assertEditState();
    // Resume to directing state
    onView(withId(R.id.cancelButton)).perform(click());
    assertCameraState();
  }

  @Test public void recordVideoWithButton() throws Exception {
    // Start recording.
    recodeVideo();
    // Should proceed to edit state
    assertEditState();
    onView(withId(R.id.videoTextureView)).check(matches(isFullScreen()));
  }

  @Test public void recordVideoAndResumeDirecting() throws Exception {
    // Start recording.
    recodeVideo();
    // Should proceed to edit state
    assertEditState();
    // Resume to directing state
    onView(withId(R.id.cancelButton)).perform(click());
    assertCameraState();
  }

  // Test takes forever to complete, so ignore it.
  @Ignore @Test public void notTakingPictureWhenNotAuth() throws Exception {
    mFakeAuthManager.forbidAuth();
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override public void run() {
        mMainActivityRule.getActivity().mToolbarStudio.performClick();
      }
    });
    // Ensuring signing in Toast is shown.
    onView(withText(R.string.signing_in)).inRoot(
        withDecorView(not(mMainActivityRule.getActivity().getWindow().getDecorView())))
        .check(matches(isDisplayed()));
    // Should navigate to welcome activity
    waitForActivity(WelcomeActivity.class);
  }

  @Test public void editStateSaved() throws Exception {
    // Take a picture
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override public void run() {
        mMainActivityRule.getActivity().mToolbarStudio.performClick();
      }
    });
    assertEditState();
    // Chose a reaction
    onView(withId(mStudioFragment.getEmotionToViewId().get(Emotion.SURPRISE))).perform(click());
    // Should proceed to camera state.
    assertCameraState();
    // Record a video
    recodeVideo();
    // Should proceed to edit state
    assertEditState();
    Scene directedScene =
        ((StudioFragment) mMainActivityRule.getActivity().getCurrentMainFragment()).getViewModel()
            .getDirectedScene();
    // Navigate out of studio
    onView(withId(android.R.id.content)).perform(ViewActions.swipeLeft());
    waitForMainFragment(MainActivity.TOOLBAR_THEATER_INDEX);
    // Navigate back to studio
    onView(withId(android.R.id.content)).perform(ViewActions.swipeRight());
    waitForMainFragment(MainActivity.TOOLBAR_STUDIO_INDEX);
    // Should resume to edit state
    assertEditState();
    // Should save the directed scene
    assertEquals(directedScene,
        ((StudioFragment) mMainActivityRule.getActivity().getCurrentMainFragment()).getViewModel()
            .getDirectedScene());
  }

  @Test public void cameraStateSaved() throws Exception {
    // Take a picture
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override public void run() {
        mMainActivityRule.getActivity().mToolbarStudio.performClick();
      }
    });
    assertEditState();
    // Chose a reaction
    onView(withId(mStudioFragment.getEmotionToViewId().get(Emotion.SURPRISE))).perform(click());
    // Should proceed to camera state.
    assertCameraState();
    Scene directedScene =
        ((StudioFragment) mMainActivityRule.getActivity().getCurrentMainFragment()).getViewModel()
            .getDirectedScene();
    // Navigate out of studio
    onView(withId(android.R.id.content)).perform(ViewActions.swipeLeft());
    waitForMainFragment(MainActivity.TOOLBAR_THEATER_INDEX);
    // Navigate back to studio
    onView(withId(android.R.id.content)).perform(ViewActions.swipeRight());
    waitForMainFragment(MainActivity.TOOLBAR_STUDIO_INDEX);
    // Should resume to camera state
    assertCameraState();
    // Should save the directed scene
    assertEquals(directedScene,
        ((StudioFragment) mMainActivityRule.getActivity().getCurrentMainFragment()).getViewModel()
            .getDirectedScene());
    // Should save the chosen reaction
    assertEquals(Emotion.SURPRISE,
        ((StudioFragment) mMainActivityRule.getActivity().getCurrentMainFragment()).getViewModel()
            .getChosenReaction());
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
    // Toolbar should be displayed
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(1f, mMainActivityRule.getActivity().mToolbarLayout.getAlpha(), 0.01);
        assertEquals(0f, mMainActivityRule.getActivity().mToolbarLayout.getTranslationY(), 0.01);
      }
    });
    // Capture buttons are displayed.
    waitMatcher(allOf(withId(R.id.toolbar_studio), isDisplayed()));
    onView(withId(R.id.switchCameraButton)).check(matches(isDisplayed()));
    // Approval buttons are hidden
    onView(withId(R.id.sendButton)).check(matches(not(isDisplayed())));
    onView(withId(R.id.cancelButton)).check(matches(not(isDisplayed())));
    // Loading image is hidden
    assertEquals(GONE, mStudioFragment.mLoadingImage.getVisibility());
    // Directed scene preview is hidden
    onView(withId(R.id.previewContainer)).check(matches(not(isDisplayed())));
  }

  private void assertSentState(int expectedRequestsMade) {
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(SENT, mStudioFragment.getViewModel().getState());
      }
    });
    // Wait until camera preview is frozen.
    assertNotEquals(CameraFragment.CameraState.PREVIEW, mCameraFragment.getState());
    // Buttons are hidden
    waitMatcher(allOf(withId(R.id.sendButton), not(isDisplayed())));
    onView(withId(R.id.cancelButton)).check(matches(not(isDisplayed())));
    onView(withId(R.id.switchCameraButton)).check(matches(not(isDisplayed())));
    // Toolbar should be hidden
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(0f, mMainActivityRule.getActivity().mToolbarLayout.getAlpha(), 0.01);
      }
    });
    // Loading image is show
    assertEquals(VISIBLE, mStudioFragment.mLoadingImage.getVisibility());
    // Directed scene preview is shown
    onView(withId(R.id.previewContainer)).check(matches(isDisplayed()));
    // Network request had been made
    assertEquals(expectedRequestsMade, mDispatcher.getCount(StudioApi.PATH));
  }

  private void assertPublishedState() {
    await().atMost(TIMEOUT.plus(TIMEOUT)).untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(MainActivity.TOOLBAR_THEATER_INDEX,
            mMainActivityRule.getActivity().getMainPager().getCurrentItem());
      }
    });
    // Should display toast
    onView(withText(R.string.saved_successfully)).inRoot(
        withDecorView(not(mMainActivityRule.getActivity().getWindow().getDecorView())))
        .check(matches(isDisplayed()));
  }

  private void recodeVideo() throws Exception {
    CoordinatesProvider coordinatesProvider = new CoordinatesProvider() {
      @Override public float[] calculateCoordinates(View view) {
        float[] coordinates = new float[2];
        View toolbarStudio = ((MainActivity) getCurrentActivity()).mToolbarStudio;
        coordinates[0] = getAbsoluteLeft(toolbarStudio) + toolbarStudio.getWidth() / 2;
        coordinates[1] = getAbsoluteTop(toolbarStudio) + toolbarStudio.getHeight() / 2;
        return coordinates;
      }
    };
    onView(withId(android.R.id.content)).perform(
        new GeneralClickAction(new RecordTapper(), coordinatesProvider, Press.FINGER));
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