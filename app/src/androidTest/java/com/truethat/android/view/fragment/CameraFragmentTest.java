package com.truethat.android.view.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.support.test.filters.FlakyTest;
import android.support.v4.app.FragmentTransaction;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.application.permissions.DevicePermissionsManager;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.common.util.CameraUtil;
import com.truethat.android.view.activity.TestActivity;
import java.io.File;
import java.util.concurrent.Callable;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.isDebugging;
import static com.truethat.android.application.ApplicationTestUtil.isFullScreen;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 23/06/2017 for TrueThat.
 */
public class CameraFragmentTest extends BaseApplicationTestSuite {
  private static final String FRAGMENT_TAG = "TestCameraFragment";
  private static final int VIDEO_DURATION_MILLIS = 500;
  private CameraFragment mCameraFragment;
  private boolean mImageTaken;
  private String mVideoPath;

  @Override public void setUp() throws Exception {
    super.setUp();
    mImageTaken = false;
    mVideoPath = null;
    if (!isDebugging()) Awaitility.setDefaultTimeout(Duration.FIVE_SECONDS);
    FragmentTransaction fragmentTransaction;
    // Remove existing fragments.
    if (mActivityTestRule.getActivity().getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG)
        != null) {
      fragmentTransaction =
          mActivityTestRule.getActivity().getSupportFragmentManager().beginTransaction();
      fragmentTransaction.remove(mActivityTestRule.getActivity()
          .getSupportFragmentManager()
          .findFragmentByTag(FRAGMENT_TAG));
      fragmentTransaction.commit();
    }
    // Adds new fragment
    CameraFragment cameraFragment = CameraFragment.newInstance();
    fragmentTransaction =
        mActivityTestRule.getActivity().getSupportFragmentManager().beginTransaction();
    fragmentTransaction.replace(R.id.activityRootView, cameraFragment, FRAGMENT_TAG);
    fragmentTransaction.commitAllowingStateLoss();
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        mCameraFragment = (CameraFragment) mActivityTestRule.getActivity()
            .getSupportFragmentManager()
            .findFragmentByTag(FRAGMENT_TAG);
        return mCameraFragment != null;
      }
    });
    mCameraFragment.setCameraFragmentListener(new TestCameraFragmentListener());
    // Wait until the camera is opened.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mCameraFragment.isCameraOpen();
      }
    });
  }

  @SuppressWarnings("ResultOfMethodCallIgnored") @Override public void tearDown() throws Exception {
    super.tearDown();
    try {
      File video = new File(mVideoPath);
      video.delete();
    } catch (Exception ignored) {
    }
  }

  @Test public void pictureTakenWithFrontCamera() throws Exception {
    // Wait for preview to be available.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mCameraFragment.getCameraPreview().isAvailable();
      }
    });
    mCameraFragment.takePicture();
    // An image should have been taken.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mImageTaken;
      }
    });
  }

  @Test public void pictureTakenWithBackCamera() throws Exception {
    // Switch camera
    mCameraFragment.switchCamera();
    // Wait until camera is opened with back camera.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mCameraFragment.isCameraOpen()
            && mCameraFragment.getFacing() == CameraUtil.Facing.BACK;
      }
    });
    // Taking a picture.
    mCameraFragment.takePicture();
    // An image should have been taken.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mImageTaken;
      }
    });
  }

  @Test public void recordVideoWithFrontCamera() throws Exception {
    // Wait for preview to be available.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mCameraFragment.getCameraPreview().isAvailable();
      }
    });
    AppContainer.setPermissionsManager(
        new DevicePermissionsManager(mActivityTestRule.getActivity().getApplication()));
    AppContainer.getPermissionsManager()
        .requestIfNeeded(mCameraFragment.getActivity(), Permission.RECORD_AUDIO);
    mCameraFragment.startRecordVideo();
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mCameraFragment.isRecordingVideo());
      }
    });
    Thread.sleep(VIDEO_DURATION_MILLIS);
    mCameraFragment.stopRecordVideo();
    // A video should have been recorded.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertFalse(mCameraFragment.isRecordingVideo());
        assertNotNull(mVideoPath);
        File videoFile = new File(mVideoPath);
        assertTrue(videoFile.exists());
      }
    });
  }

  @Test public void recordVideoWithBackCamera() throws Exception {
    // Switch camera
    mCameraFragment.switchCamera();
    // Wait until camera is opened with back camera.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mCameraFragment.isCameraOpen()
            && mCameraFragment.getFacing() == CameraUtil.Facing.BACK;
      }
    });
    mCameraFragment.startRecordVideo();
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mCameraFragment.isRecordingVideo());
      }
    });
    Thread.sleep(VIDEO_DURATION_MILLIS);
    mCameraFragment.stopRecordVideo();
    // A video should have been recorded.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertFalse(mCameraFragment.isRecordingVideo());
        assertNotNull(mVideoPath);
        File videoFile = new File(mVideoPath);
        assertTrue(videoFile.exists());
      }
    });
  }

  @Test public void pictureNotTaken_activityPaused() throws Exception {
    mCameraFragment.takePicture();
    // Navigate to a different activity
    mActivityTestRule.getActivity()
        .startActivity(new Intent(mActivityTestRule.getActivity(), TestActivity.class));
    // Wait for an image to be taken
    Thread.sleep(TIMEOUT.getValueInMS());
    // An image should not have been taken.
    assertFalse(mImageTaken);
  }

  @Test public void pictureNotTaken_cameraClosed() throws Exception {
    mCameraFragment.takePicture();
    // Close camera
    mCameraFragment.onHidden();
    // Wait for an image to be taken
    Thread.sleep(TIMEOUT.getValueInMS());
    // An image should not have been taken.
    assertFalse(mImageTaken);
  }

  @Test public void cameraPreviewIsFullscreen() throws Exception {
    // Wait until preview is available.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mCameraFragment.getCameraPreview().isAvailable();
      }
    });
    // Preview should be full screen
    onView(withId(R.id.cameraPreview)).check(matches(isFullScreen()));
  }

  // Test is flaky in real devices
  @Test @FlakyTest public void cameraPreviewIsFrozenAfterTakingPicture() throws Exception {
    mCameraFragment.takePicture();
    // An image should have been taken.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mImageTaken;
      }
    });
    // Save current preview.
    Bitmap than = mCameraFragment.getCameraPreview().getBitmap();
    // Wait for the preview to change, this is unstable, but at least something.
    Thread.sleep(500);
    Bitmap now = mCameraFragment.getCameraPreview().getBitmap();
    assertTrue(than.sameAs(now));
  }

  @Test public void cameraPreviewCanBeRestored() throws Exception {
    mCameraFragment.takePicture();
    // An image should have been taken.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mImageTaken;
      }
    });
    // Save current preview.
    Bitmap than = mCameraFragment.getCameraPreview().getBitmap();
    // Restores preview
    mCameraFragment.restorePreview();
    // Increase probability of preview change.
    Thread.sleep(TIMEOUT.getValueInMS());
    // New preview should be different
    Bitmap now = mCameraFragment.getCameraPreview().getBitmap();
    assertFalse(than.sameAs(now));
  }

  @Test public void takingMultipleImages() throws Exception {
    mCameraFragment.takePicture();
    // An image should have been taken.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mImageTaken;
      }
    });
    mImageTaken = false;
    // Restore preview
    mCameraFragment.restorePreview();
    mCameraFragment.takePicture();
    // Another image should have been taken.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mImageTaken;
      }
    });
  }

  @Test public void cameraClosedOnPause() throws Exception {
    mActivityTestRule.getActivity()
        .startActivity(new Intent(mActivityTestRule.getActivity(), TestActivity.class));
    // Camera should be closed.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return !mCameraFragment.isCameraOpen();
      }
    });
  }

  private class TestCameraFragmentListener implements CameraFragment.CameraFragmentListener {
    @Override public void onImageAvailable(Image image) {
      mImageTaken = true;
    }

    @Override public void onVideoAvailable(String videoPath) {
      mVideoPath = videoPath;
    }

    @Override public void onVideoRecordStart() {
    }
  }
}