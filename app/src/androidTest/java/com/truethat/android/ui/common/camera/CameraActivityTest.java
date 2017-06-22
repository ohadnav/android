package com.truethat.android.ui.common.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ImageReader;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import com.truethat.android.common.BaseApplicationTest;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.ui.common.AskForPermissionActivity;
import com.truethat.android.ui.common.TestActivity;
import com.truethat.android.ui.studio.StudioActivity;
import com.truethat.android.ui.theater.TheaterActivity;
import java.util.Collections;
import java.util.concurrent.Callable;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */
@RunWith(AndroidJUnit4.class) public class CameraActivityTest extends BaseApplicationTest {
  @Rule public ActivityTestRule<StudioActivity> mStudioActivityTestRule =
      new ActivityTestRule<>(StudioActivity.class, true, false);
  @Rule public ActivityTestRule<TheaterActivity> mTheaterActivityTestRule =
      new ActivityTestRule<>(TheaterActivity.class, true, false);
  @Rule public ActivityTestRule<AskForPermissionActivity> mAskForPermissionActivityTestRule =
      new ActivityTestRule<>(AskForPermissionActivity.class, true, false);
  private boolean mImageTaken = false;
  private final ImageReader.OnImageAvailableListener IMAGE_AVAILABLE_LISTENER =
      new ImageReader.OnImageAvailableListener() {
        @Override public void onImageAvailable(ImageReader reader) {
          mImageTaken = true;
        }
      };

  @Before public void setUp() throws Exception {
    super.setUp();
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        return new MockResponse().setResponseCode(200)
            .setBody(NetworkUtil.GSON.toJson(Collections.EMPTY_LIST) + "\n");
      }
    });
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

  @Test @MediumTest public void cameraPreviewIsFrozenAfterTakingPicture() throws Exception {
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
    // Save current preview.
    Bitmap than = mStudioActivityTestRule.getActivity().getCameraPreview().getBitmap();
    // Wait for the preview to change, this is unstable, but at least something.
    Thread.sleep(500);
    Bitmap now = mStudioActivityTestRule.getActivity().getCameraPreview().getBitmap();
    assertTrue(than.sameAs(now));
  }

  @Test public void takingMultipleImages() throws Exception {
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

  @Test public void cameraClosedOnPause() throws Exception {
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