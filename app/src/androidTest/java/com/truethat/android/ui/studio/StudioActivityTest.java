package com.truethat.android.ui.studio;

import android.media.Image;
import android.media.ImageReader;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.empathy.Emotion;
import com.truethat.android.model.Scene;
import com.truethat.android.ui.common.camera.CameraFragment;
import com.truethat.android.ui.theater.TheaterActivity;
import java.util.Date;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
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
import static com.truethat.android.application.ApplicationTestUtil.waitForActivity;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.not;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */
public class StudioActivityTest extends BaseApplicationTestSuite {
  private static final long SCENE_ID = 123L;
  @Rule public ActivityTestRule<StudioActivity> mStudioActivityTestRule =
      new ActivityTestRule<>(StudioActivity.class, true, false);
  private Image mImageMock;
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
  }

  @Test @MediumTest public void takePictureWithButton() throws Exception {
    // Modifies image listener, so that images are not sent.
    mCameraFragment.setOnImageAvailableListener(IMAGE_AVAILABLE_LISTENER);
    // Takes a picture.
    onView(withId(R.id.captureButton)).perform(click());
    // Wait until camera is opened
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mCameraFragment.isCameraOpen();
      }
    });
    // Wait until an image is taken
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mImageTaken;
      }
    });
  }

  @Test @MediumTest public void notTakingPictureWhenNotAuth() throws Exception {
    mMockAuthModule.setAllowAuth(false);
    onView(withId(R.id.captureButton)).perform(click());
    // Ensuring signing in Toast is shown.
    onView(withText(mStudioActivityTestRule.getActivity().UNAUTHORIZED_TOAST)).inRoot(
        withDecorView(not(mStudioActivityTestRule.getActivity().getWindow().getDecorView())))
        .check(matches(isDisplayed()));
  }

  // -------------------------- StudioAPI tests --------------------------------
  @Test public void studioAPI_imageSent() throws Exception {
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        Scene respondedScene =
            new Scene(SCENE_ID, "", App.getAuthModule().getUser(), new TreeMap<Emotion, Long>(),
                new Date(), null);
        return new MockResponse().setBody(NetworkUtil.GSON.toJson(respondedScene) + "\n");
      }
    });
    // Take a picture
    onView(withId(R.id.captureButton)).perform(click());
    // Should navigate to theater
    waitForActivity(TheaterActivity.class);
  }
}