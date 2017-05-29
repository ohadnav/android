package com.truethat.android.studio;

import android.media.Image;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;

import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.application.permissions.DefaultPermissionsModule;
import com.truethat.android.application.permissions.MockPermissionsModule;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.application.storage.internal.MockInternalStorage;
import com.truethat.android.common.camera.CameraTestUtil;
import com.truethat.android.common.camera.CameraUtil;
import com.truethat.android.common.util.AssetsReaderUtil;
import com.truethat.android.identity.MockAuthModule;
import com.truethat.android.test.BuildConfig;
import com.truethat.android.theater.Scene;

import org.awaitility.Awaitility;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.studio.StudioActivity.CREATED_SCENES_PATH;
import static com.truethat.android.studio.StudioActivity.SCENE_SUFFIX;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */
public class StudioActivityTest {
    private static final long                             SCENE_ID                = 123L;
    private final        MockWebServer                    mMockWebServer          = new MockWebServer();
    @Rule
    public               ActivityTestRule<StudioActivity> mStudioActivityTestRule =
            new ActivityTestRule<>(StudioActivity.class, true, true);
    private Image mImageMock;

    @BeforeClass
    public static void beforeClass() throws Exception {
        // Sets up the mocked permissions module.
        App.setPermissionsModule(new MockPermissionsModule(Permission.CAMERA));
        // Sets up the mocked auth module.
        App.setAuthModule(new MockAuthModule());
    }

    @AfterClass
    public static void afterClass() throws Exception {
        // Resets to default permissions module.
        App.setPermissionsModule(new DefaultPermissionsModule());
    }

    @Before
    public void setUp() throws Exception {
        // Initialize Awaitility
        Awaitility.reset();
        // Starts mock server
        mMockWebServer.start(BuildConfig.PORT);
        // Initializes the mocked image.
        mImageMock = CameraTestUtil.bitmapBytesToMockedImage(AssetsReaderUtil.readAsBytes(
                mStudioActivityTestRule.getActivity(), CameraTestUtil.BITMAP_1x1_PATH), 0);
        // Sets up new mocked internal storage.
        App.setInternalStorage(new MockInternalStorage());
    }

    @Test
    @MediumTest
    public void takePictureWithButton() throws Exception {
        onView(withId(R.id.captureButton)).perform(click());
        await().untilAsserted(
                () -> assertNotNull(mStudioActivityTestRule.getActivity().supplyImage()));
    }

    // -------------------------- StudioAPI tests --------------------------------
    @Test
    public void studioAPI_imageSent() throws Exception {
        final Dispatcher dispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                // TODO(ohad): assert request parameters.
                return new MockResponse().setResponseCode(200).setBody(Long.toString(SCENE_ID));
            }
        };
        // Ensures the image taken is {@code mImageMock}.
        mStudioActivityTestRule.getActivity().setImageSupplier(() -> mImageMock);
        mMockWebServer.setDispatcher(dispatcher);
        mStudioActivityTestRule.getActivity().processImage();
        // Wait until scene is saved to internal memory.
        await().untilAsserted(
                () -> assertTrue(App.getInternalStorage().exists(
                        mStudioActivityTestRule.getActivity(),
                        CREATED_SCENES_PATH + SCENE_ID + SCENE_SUFFIX)));
        Scene scene = App.getInternalStorage().read(mStudioActivityTestRule.getActivity(),
                                                    CREATED_SCENES_PATH + SCENE_ID + SCENE_SUFFIX);
        // Asserts the saved image is the one captured by the camera (i.e. mImageMock)
        assertTrue(CameraUtil.compare(
                CameraTestUtil.bitmapBytesToMockedImage(scene.getImageBytes(),
                                                        scene.getTimestamp().getTime()),
                mImageMock));
        assertEquals(SCENE_ID, scene.getId());
        assertEquals(App.getAuthModule().getCurrentUser().getId(), scene.getCreator().getId());
    }
}