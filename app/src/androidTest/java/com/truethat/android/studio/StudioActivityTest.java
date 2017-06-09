package com.truethat.android.studio;

import android.media.Image;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import com.google.common.base.Supplier;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.application.permissions.DefaultPermissionsModule;
import com.truethat.android.application.permissions.MockPermissionsModule;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.application.storage.internal.MockInternalStorage;
import com.truethat.android.common.Scene;
import com.truethat.android.common.camera.CameraTestUtil;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.util.AssetsReaderUtil;
import com.truethat.android.empathy.Emotion;
import com.truethat.android.identity.MockAuthModule;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.BuildConfig.BACKEND_URL;
import static com.truethat.android.BuildConfig.PORT;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */
public class StudioActivityTest {
  private static final long SCENE_ID = 123L;
  private final MockWebServer mMockWebServer = new MockWebServer();
  @Rule public ActivityTestRule<StudioActivity> mStudioActivityTestRule =
      new ActivityTestRule<>(StudioActivity.class, true, true);
  private Image mImageMock;

  @BeforeClass public static void beforeClass() throws Exception {
    // Sets up the mocked permissions module.
    App.setPermissionsModule(new MockPermissionsModule(Permission.CAMERA));
    // Sets up the mocked auth module.
    App.setAuthModule(new MockAuthModule());
    // Sets the backend URL, for MockWebServer.
    NetworkUtil.setBackendUrl("http://localhost");
  }

  @AfterClass public static void afterClass() throws Exception {
    // Resets to default permissions module.
    App.setPermissionsModule(new DefaultPermissionsModule());
    // Restores default predefined backend url.
    NetworkUtil.setBackendUrl(BACKEND_URL);
  }

  @Before public void setUp() throws Exception {
    // Initialize Awaitility
    Awaitility.reset();
    // Starts mock server
    mMockWebServer.start(PORT);
    // Initializes the mocked image.
    mImageMock = CameraTestUtil.bitmapBytesToMockedImage(
        AssetsReaderUtil.readAsBytes(mStudioActivityTestRule.getActivity(), CameraTestUtil.BITMAP_1x1_PATH), 0);
    // Sets up new mocked internal storage.
    App.setInternalStorage(new MockInternalStorage());
  }

  @After public void tearDown() throws Exception {
    // Closes mock server
    mMockWebServer.close();
  }

  @Test @MediumTest public void takePictureWithButton() throws Exception {
    onView(withId(R.id.captureButton)).perform(click());
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mStudioActivityTestRule.getActivity().supplyImage() != null;
      }
    });
  }

  // -------------------------- StudioAPI tests --------------------------------
  @Test(timeout = 3000) public void studioAPI_imageSent() throws Exception {
    final Dispatcher dispatcher = new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        // TODO(ohad): test request content
        Scene respondedScene =
            new Scene(SCENE_ID, "", App.getAuthModule().getCurrentUser(), new TreeMap<Emotion, Long>(), new Date(),
                null);
        // "\n" is needed at the end to imply response EOF.
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(NetworkUtil.GSON.toJson(respondedScene) + "\n");
      }
    };
    // Ensures the image taken is {@code mImageMock}.
    mStudioActivityTestRule.getActivity().setImageSupplier(new Supplier<Image>() {
      @Override public Image get() {
        return mImageMock;
      }
    });
    mMockWebServer.setDispatcher(dispatcher);
    mStudioActivityTestRule.getActivity().processImage();
    // Wait until scene is saved to internal memory.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return App.getInternalStorage()
            .exists(mStudioActivityTestRule.getActivity(), Scene.internalStoragePath(SCENE_ID));
      }
    });
    Scene scene =
        App.getInternalStorage().read(mStudioActivityTestRule.getActivity(), Scene.internalStoragePath(SCENE_ID));
    assertEquals(SCENE_ID, scene.getId());
  }
}