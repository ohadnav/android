package com.truethat.android.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ImageReader;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.filters.FlakyTest;
import android.support.test.rule.ActivityTestRule;
import com.truethat.android.R;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.network.StudioApi;
import com.truethat.android.common.util.CountingDispatcher;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Scene;
import com.truethat.android.ui.common.camera.CameraFragment;
import java.net.HttpURLConnection;
import java.util.TreeMap;
import java.util.concurrent.Callable;
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
import static com.truethat.android.application.ApplicationTestUtil.centerSwipeUp;
import static com.truethat.android.application.ApplicationTestUtil.waitForActivity;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */
public class StudioActivityTest extends BaseApplicationTestSuite {
  @Rule public ActivityTestRule<StudioActivity> mStudioActivityTestRule =
      new ActivityTestRule<>(StudioActivity.class, true, false);
  private boolean mImageTaken = false;
  private final ImageReader.OnImageAvailableListener IMAGE_AVAILABLE_LISTENER =
      new ImageReader.OnImageAvailableListener() {
        @Override public void onImageAvailable(ImageReader reader) {
          mImageTaken = true;
        }
      };
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
        return new MockResponse().setBody(NetworkUtil.GSON.toJson(
            new Scene(1, null, null, new TreeMap<Emotion, Long>(), null, null)));
      }
    });
  }

  @Test public void takePictureWithButton() throws Exception {
    // Modifies image listener, so that images are not sent.
    mCameraFragment.setOnImageAvailableListener(IMAGE_AVAILABLE_LISTENER);
    // Take a picture.
    onView(withId(R.id.captureButton)).perform(click());
    // Wait until an image is taken
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mImageTaken;
      }
    });
  }

  @Test public void notTakingPictureWhenNotAuth() throws Exception {
    mMockAuthModule.setAllowAuth(false);
    onView(withId(R.id.captureButton)).perform(click());
    assertDirectingState();
    // Ensuring signing in Toast is shown.
    onView(withText(mStudioActivityTestRule.getActivity().UNAUTHORIZED_TOAST)).inRoot(
        withDecorView(not(mStudioActivityTestRule.getActivity().getWindow().getDecorView())))
        .check(matches(isDisplayed()));
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
    Bitmap than = mCameraFragment.getCameraPreview().getBitmap();
    // Navigate out of studio
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeDown());
    waitForActivity(TheaterActivity.class);
    // Navigate back to studio
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeUp());
    waitForActivity(StudioActivity.class);
    // Should remain in approval state
    assertApprovalState();
    // Preview should remain frozen.
    Bitmap now = mCameraFragment.getCameraPreview().getBitmap();
    assertTrue(than.sameAs(now));
  }

  @Test public void directingState() throws Exception {
    assertDirectingState();
  }

  @Test public void switchCamera() throws Exception {
    // Switch front with back camera
    onView(withId(R.id.switchCameraButton)).perform(click());
    assertDirectingState();
  }

  @Test public void approvalState() throws Exception {
    // Take a picture
    onView(withId(R.id.captureButton)).perform(click());
    assertApprovalState();
  }

  @Test public void approvalCancel() throws Exception {
    // Take a picture
    onView(withId(R.id.captureButton)).perform(click());
    assertApprovalState();
    // Cancel the picture taken
    onView(withId(R.id.cancelButton)).perform(click());
    assertDirectingState();
  }

  @Test public void sentState() throws Exception {
    // Take a picture
    onView(withId(R.id.captureButton)).perform(click());
    assertApprovalState();
    // Send the reactable.
    onView(withId(R.id.sendButton)).perform(click());
    assertSentState();
  }

  @Test public void publishedState() throws Exception {
    // Take a picture
    onView(withId(R.id.captureButton)).perform(click());
    assertApprovalState();
    // Send the reactable.
    onView(withId(R.id.sendButton)).perform(click());
    assertSentState();
    assertPublishedState();
  }

  @Test public void publishedFailed() throws Exception {
    setDispatcher(new CountingDispatcher() {
      @Override public MockResponse processRequest(RecordedRequest request) throws Exception {
        Thread.sleep(BaseApplicationTestSuite.TIMEOUT.getValueInMS() / 2);
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
      }
    });
    // Take a picture
    onView(withId(R.id.captureButton)).perform(click());
    assertApprovalState();
    // Send the reactable.
    onView(withId(R.id.sendButton)).perform(click());
    assertSentState();
    // Should fail
    assertPublishFailed();
  }

  @Test public void activityPausedWhileSending() throws Exception {
    // Take a picture
    onView(withId(R.id.captureButton)).perform(click());
    assertApprovalState();
    // Send the reactable.
    onView(withId(R.id.sendButton)).perform(click());
    assertSentState();
    // Pause activity.
    mStudioActivityTestRule.getActivity()
        .startActivity(new Intent(mStudioActivityTestRule.getActivity(), TestActivity.class));
    // Hit back
    Espresso.pressBack();
    // Should fail
    assertPublishFailed();
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
    onView(withId(R.id.loadingImage)).check(matches(not(isDisplayed())));
    // Preview should have no tint
    assertNull(mCameraFragment.getCameraPreview().getBackgroundTintList());
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
    onView(withId(R.id.loadingImage)).check(matches(not(isDisplayed())));
    // Preview should have no tint
    assertNull(mCameraFragment.getCameraPreview().getBackgroundTintList());
  }

  private void assertSentState() {
    // Capture and approval buttons are hidden.
    onView(withId(R.id.cancelButton)).check(matches(not(isDisplayed())));
    onView(withId(R.id.switchCameraButton)).check(matches(not(isDisplayed())));
    onView(withId(R.id.sendButton)).check(matches(not(isDisplayed())));
    onView(withId(R.id.captureButton)).check(matches(not(isDisplayed())));
    // Should have a tint
    assertEquals(mStudioActivityTestRule.getActivity().getColorStateList(R.color.tint),
        mCameraFragment.getCameraPreview().getBackgroundTintList());
    // Loading image should be visible
    onView(withId(R.id.loadingImage)).check(matches(isDisplayed()));
  }

  private void assertPublishedState() {
    // Should post the reactable.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(1, mDispatcher.getCount(StudioApi.PATH));
      }
    });
    // Should navigate to theater.
    waitForActivity(TheaterActivity.class);
  }

  private void assertPublishFailed() {
    // Should return to approval.
    assertApprovalState();
    // Should show failure Toast.
    onView(withText(mStudioActivityTestRule.getActivity().SENT_FAILED)).inRoot(
        withDecorView(not(mStudioActivityTestRule.getActivity().getWindow().getDecorView())))
        .check(matches(isDisplayed()));
  }
}