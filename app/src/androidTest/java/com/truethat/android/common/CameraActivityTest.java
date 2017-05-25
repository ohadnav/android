package com.truethat.android.common;

import android.content.ComponentName;
import android.content.Intent;
import android.support.test.espresso.intent.Intents;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.truethat.android.application.App;
import com.truethat.android.application.permissions.MockPermissionsModule;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.studio.StudioActivity;
import com.truethat.android.thatre.TheaterActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */
@RunWith(AndroidJUnit4.class)
public class CameraActivityTest {
    @Rule
    public  ActivityTestRule<StudioActivity>  mStudioActivityTestRule  =
            new ActivityTestRule<>(StudioActivity.class, true, false);
    @Rule
    public  ActivityTestRule<TheaterActivity> mTheaterActivityTestRule =
            new ActivityTestRule<>(TheaterActivity.class, true, false);
    private MockPermissionsModule             mPermissionsModule       = new MockPermissionsModule(
            Permission.CAMERA);

    @Before
    public void setUp() throws Exception {
        Intents.init();
        // Sets up the mocked permissions module.
        App.setPermissionsModule(mPermissionsModule);
    }

    @After
    public void tearDown() throws Exception {
        Intents.release();
    }

    @Test
    public void onRequestPermissions_permissionGranted() throws Exception {
        mStudioActivityTestRule.launchActivity(null);
        // If we are still in CameraActivity, then permission was granted.
        intended(hasComponent(new ComponentName(getTargetContext(), StudioActivity.class)));
    }

    @Test
    public void onRequestPermissionsFailed() throws Exception {
        // Don't grant permission.
        mPermissionsModule.revokeAndForbid(Permission.CAMERA);
        mStudioActivityTestRule.launchActivity(null);
        // Should navigate to NoCameraPermissionActivity.
        intended(hasComponent(
                new ComponentName(getTargetContext(), NoCameraPermissionActivity.class)));
        // Grant permission
        mPermissionsModule.grant(Permission.CAMERA);
    }

    @Test
    @MediumTest
    public void takePicture_noSurfaceTexture() throws Exception {
        mTheaterActivityTestRule.launchActivity(null);
        assertNull(mTheaterActivityTestRule.getActivity().getLastTakenImage());
        mTheaterActivityTestRule.getActivity().takePicture();
        Thread.sleep(1000);
        assertNotNull(mTheaterActivityTestRule.getActivity().getLastTakenImage());
    }

    @Test
    @MediumTest
    public void takePicture_withSurfaceTexture() throws Exception {
        mStudioActivityTestRule.launchActivity(null);
        assertNull(mStudioActivityTestRule.getActivity().getLastTakenImage());
        mStudioActivityTestRule.getActivity().takePicture();
        Thread.sleep(1000);
        assertNotNull(mStudioActivityTestRule.getActivity().getLastTakenImage());
    }

    @Test
    public void onPause() throws Exception {
        mStudioActivityTestRule.launchActivity(null);
        // Navigates to an activity without camera.
        mStudioActivityTestRule.getActivity().startActivity(
                new Intent(mStudioActivityTestRule.getActivity(),
                           NoCameraPermissionActivity.class));
        Thread.sleep(1000);
        // Asserts the camera is closed.
        assertNull(mStudioActivityTestRule.getActivity().getCameraDevice());
    }
}