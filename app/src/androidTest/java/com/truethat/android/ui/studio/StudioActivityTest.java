package com.truethat.android.ui.studio;

import android.media.Image;
import android.media.ImageReader;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.util.AssetsReaderUtil;
import com.truethat.android.common.util.CameraTestUtil;
import com.truethat.android.empathy.Emotion;
import com.truethat.android.model.Scene;
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
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;

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

  @Before public void setUp() throws Exception {
    super.setUp();
    // Launches studio activity.
    mStudioActivityTestRule.launchActivity(null);
    // Initializes the mocked image.
    mImageMock = CameraTestUtil.bitmapBytesToMockedImage(
        AssetsReaderUtil.readAsBytes(mStudioActivityTestRule.getActivity(),
            CameraTestUtil.BITMAP_1x1_PATH), 0);
    mStudioActivityTestRule.getActivity()
        .getCameraFragment()
        .setOnImageAvailableListener(IMAGE_AVAILABLE_LISTENER);
  }

  @Test @MediumTest public void takePictureWithButton() throws Exception {
    onView(withId(R.id.captureButton)).perform(click());
    // Wait until camera is opened
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mStudioActivityTestRule.getActivity().getCameraFragment().isCameraOpen();
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
    final Dispatcher dispatcher = new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        // TODO(ohad): test request content
        Scene respondedScene =
            new Scene(SCENE_ID, "", App.getAuthModule().getUser(), new TreeMap<Emotion, Long>(),
                new Date(), null);
        // "\n" is needed at the end to imply response EOF.
        return new MockResponse().setBody(NetworkUtil.GSON.toJson(respondedScene) + "\n");
      }
    };
    mMockWebServer.setDispatcher(dispatcher);
    //noinspection ConstantConditions
    mStudioActivityTestRule.getActivity().processImage(mImageMock);
    // Wait until scene is saved to internal memory.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return App.getInternalStorage()
            .exists(mStudioActivityTestRule.getActivity(), Scene.internalStoragePath(SCENE_ID));
      }
    });
    Scene scene = App.getInternalStorage()
        .read(mStudioActivityTestRule.getActivity(), Scene.internalStoragePath(SCENE_ID));
    assertEquals(SCENE_ID, scene.getId());
  }
}