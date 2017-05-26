package com.truethat.android.studio;

import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;

import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.application.permissions.DefaultPermissionsModule;
import com.truethat.android.application.permissions.MockPermissionsModule;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.common.util.AssetsReaderUtil;
import com.truethat.android.test.BuildConfig;

import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */
public class StudioActivityTest {
    private final MockWebServer                    mMockWebServer          = new MockWebServer();
    @Rule
    public        ActivityTestRule<StudioActivity> mStudioActivityTestRule =
            new ActivityTestRule<>(StudioActivity.class, true, true);

    @Before
    public void setUp() throws Exception {
        // Initialize Awaitility
        Awaitility.reset();
        // Sets up the mocked permissions module.
        App.setPermissionsModule(new MockPermissionsModule(Permission.CAMERA));
        // Starts mock server
        mMockWebServer.start(BuildConfig.PORT);
    }

    @After
    public void tearDown() throws Exception {
        // Resets to default permissions module.
        App.setPermissionsModule(new DefaultPermissionsModule());
    }

    @Test(timeout = 3000)
    @MediumTest
    public void takePictureWithButton() throws Exception {
        onView(withId(R.id.captureButton)).perform(click());
        await().untilAsserted(
                () -> assertNotNull(mStudioActivityTestRule.getActivity().getLastTakenImage()));
    }

    // -------------------------- StudioAPI tests --------------------------------
    @Test(timeout = 3000)
    public void studioAPI_imageSent() throws Exception {
        final Dispatcher dispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                assertEquals("/studio", request.getPath());
                assertEquals("POST", request.getMethod());
                return new MockResponse().setResponseCode(200).setBody(
                        AssetsReaderUtil.read(mStudioActivityTestRule.getActivity(),
                                              "studio/save_scene_response_success.json"));
            }
        };
        onView(withId(R.id.captureButton)).perform(click());
        mMockWebServer.setDispatcher(dispatcher);
    }
}