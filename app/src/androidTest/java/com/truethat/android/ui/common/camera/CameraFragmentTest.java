package com.truethat.android.ui.common.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ImageReader;
import android.support.v4.app.FragmentTransaction;
import com.truethat.android.R;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.common.util.CameraUtil;
import com.truethat.android.ui.activity.TestActivity;
import java.util.concurrent.Callable;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.isDebugging;
import static com.truethat.android.application.ApplicationTestUtil.isFullScreen;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 23/06/2017 for TrueThat.
 */
public class CameraFragmentTest extends BaseApplicationTestSuite {
  private static final String FRAGMENT_TAG = "TestCameraFragment";
  private CameraFragment mCameraFragment;
  private boolean mImageTaken;
  private final ImageReader.OnImageAvailableListener IMAGE_AVAILABLE_LISTENER =
      new ImageReader.OnImageAvailableListener() {
        @Override public void onImageAvailable(ImageReader reader) {
          reader.acquireLatestImage();
          mImageTaken = true;
        }
      };

  @Override public void setUp() throws Exception {
    super.setUp();
    mImageTaken = false;
    if (!isDebugging()) Awaitility.setDefaultTimeout(Duration.FIVE_SECONDS);
  }

  @Test public void pictureTakenWithoutCameraPreview() throws Exception {
    init(false);
    // Preview should be hidden.
    onView(withId(R.id.cameraPreview)).check(matches(not(isDisplayed())));
    mCameraFragment.takePicture();
    // An image should be taken.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mImageTaken;
      }
    });
  }

  @Test public void pictureTakenWithCameraPreview() throws Exception {
    init(true);
    // Wait for preview to be available.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mCameraFragment.getCameraPreview().isAvailable();
      }
    });
    mCameraFragment.takePicture();
    // An image should be taken.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mImageTaken;
      }
    });
  }

  @Test public void pictureTakenWithBackCamera() throws Exception {
    init(true);
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
    // An image should be taken.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mImageTaken;
      }
    });
  }

  @Test public void switchCameraTwice() throws Exception {
    init(true);
    // Switch camera
    mCameraFragment.switchCamera();
    // Wait until camera is opened with BACK camera.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mCameraFragment.isCameraOpen()
            && mCameraFragment.getFacing() == CameraUtil.Facing.BACK;
      }
    });
    // Switch camera
    mCameraFragment.switchCamera();
    // Wait until camera is opened with FRONT camera.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mCameraFragment.isCameraOpen()
            && mCameraFragment.getFacing() == CameraUtil.Facing.FRONT;
      }
    });
    // Taking a picture.
    mCameraFragment.takePicture();
    // An image should be taken.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mImageTaken;
      }
    });
  }

  @Test public void pictureNotTaken_activityPaused() throws Exception {
    init(true);
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
    init(true);
    mCameraFragment.takePicture();
    // Close camera
    mCameraFragment.onHidden();
    // Wait for an image to be taken
    Thread.sleep(TIMEOUT.getValueInMS());
    // An image should not have been taken.
    assertFalse(mImageTaken);
  }

  @Test public void cameraPreviewIsFullscreen() throws Exception {
    init(true);
    // Wait until preview is available.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mCameraFragment.getCameraPreview().isAvailable();
      }
    });
    // Preview should be full screen
    onView(withId(R.id.cameraPreview)).check(matches(isFullScreen()));
  }

  @Test public void cameraPreviewIsFrozenAfterTakingPicture() throws Exception {
    init(true);
    mCameraFragment.takePicture();
    // An image should be taken.
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
    init(true);
    mCameraFragment.takePicture();
    // An image should be taken.
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
    init(false);
    mCameraFragment.takePicture();
    // An image should be taken.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mImageTaken;
      }
    });
    mImageTaken = false;
    mCameraFragment.takePicture();
    // Another image should be taken.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mImageTaken;
      }
    });
  }

  @Test public void cameraClosedOnPause() throws Exception {
    init(false);
    mActivityTestRule.getActivity()
        .startActivity(new Intent(mActivityTestRule.getActivity(), TestActivity.class));
    // Camera should be closed.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return !mCameraFragment.isCameraOpen();
      }
    });
  }

  private void init(boolean showPreview) {
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
    CameraFragment cameraFragment = CameraFragment.newInstance(showPreview);
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
    mCameraFragment.setOnImageAvailableListener(IMAGE_AVAILABLE_LISTENER);
    // Wait until the camera is opened.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mCameraFragment.isCameraOpen();
      }
    });
  }
}