package com.truethat.android.common.camera;

import android.content.Intent;
import android.media.ImageReader;
import android.support.test.espresso.intent.Intents;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import com.truethat.android.application.permissions.AskForPermissionActivity;
import com.truethat.android.common.BaseApplicationTest;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.util.TestActivity;
import com.truethat.android.studio.StudioActivity;
import com.truethat.android.theater.TheaterActivity;
import java.util.Collections;
import java.util.concurrent.Callable;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.truethat.android.BuildConfig.PORT;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertFalse;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */
@RunWith(AndroidJUnit4.class) public class CameraActivityApplicationTest
    extends BaseApplicationTest {
  private final MockWebServer mMockWebServer = new MockWebServer();
  @Rule public ActivityTestRule<StudioActivity> mStudioActivityTestRule =
      new ActivityTestRule<>(StudioActivity.class, true, false);
  @Rule public ActivityTestRule<TheaterActivity> mTheaterActivityTestRule =
      new ActivityTestRule<>(TheaterActivity.class, true, false);
  @Rule public ActivityTestRule<AskForPermissionActivity> mNoPermissionActivityTestRule =
      new ActivityTestRule<>(AskForPermissionActivity.class, true, false);
  private boolean mImageTaken = false;
  private final ImageReader.OnImageAvailableListener IMAGE_AVAILABLE_LISTENER =
      new ImageReader.OnImageAvailableListener() {
        @Override public void onImageAvailable(ImageReader reader) {
          mImageTaken = true;
        }
      };

  @Before public void setUp() throws Exception {
    // Initialize Awaitility
    Awaitility.reset();
    // Initialize Espresso intents
    Intents.init();
    // Starts mock server
    mMockWebServer.start(PORT);
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        return new MockResponse().setResponseCode(200)
            .setBody(NetworkUtil.GSON.toJson(Collections.EMPTY_LIST) + "\n");
      }
    });
  }
  @After public void tearDown() throws Exception {
    Intents.release();
    // Closes mock server
    mMockWebServer.close();
  }

  @Test @MediumTest public void takePicture_noSurfaceTexture() throws Exception {
    mTheaterActivityTestRule.launchActivity(null);
    mTheaterActivityTestRule.getActivity().setImageAvailableListener(IMAGE_AVAILABLE_LISTENER);
    assertFalse(mImageTaken);
    mTheaterActivityTestRule.getActivity().takePicture();
    // Assert that an image was taken.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mImageTaken;
      }
    });
  }

  @Test @MediumTest public void takePicture_withSurfaceTexture() throws Exception {
    mStudioActivityTestRule.launchActivity(null);
    mStudioActivityTestRule.getActivity().setImageAvailableListener(IMAGE_AVAILABLE_LISTENER);
    assertFalse(mImageTaken);
    mStudioActivityTestRule.getActivity().takePicture();
    // Assert that an image was taken.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mImageTaken;
      }
    });
  }

  // Test is ignored, as more than a single instance of mTheaterActivityTestRule is created.
  @Test @MediumTest @Ignore public void takePicture_cameraNotOpenedYet() throws Exception {
    mTheaterActivityTestRule.launchActivity(null);
    mTheaterActivityTestRule.getActivity().setImageAvailableListener(IMAGE_AVAILABLE_LISTENER);
    assertFalse(mImageTaken);
    // Launching a non-camera activity to close the camera.
    mNoPermissionActivityTestRule.launchActivity(null);
    // Asserts the camera is closed.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mTheaterActivityTestRule.getActivity().getCameraDevice() == null;
      }
    });
    // Trying to take a picture while the camera is closed.
    mTheaterActivityTestRule.getActivity().takePicture();
    // Launches the camera activity again.
    mNoPermissionActivityTestRule.getActivity()
        .startActivity(
            new Intent(mNoPermissionActivityTestRule.getActivity(), TheaterActivity.class));
    // Assert that an image was taken.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mImageTaken;
      }
    });
  }

  @Test public void onPause() throws Exception {
    mStudioActivityTestRule.launchActivity(null);
    // Navigates to an activity without camera.
    mStudioActivityTestRule.getActivity()
        .startActivity(new Intent(mStudioActivityTestRule.getActivity(), TestActivity.class));
    // Asserts the camera is closed.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mStudioActivityTestRule.getActivity().getCameraDevice() == null;
      }
    });
  }
}