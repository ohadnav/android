package com.truethat.android.application.permissions;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.support.v4.app.ActivityCompat;

import com.truethat.android.application.ApplicationTestUtil;
import com.truethat.android.common.TestActivity;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
@Ignore // Test fails since shell commands take time to take effect.
public class PermissionsTestUtilTest {
    private static int                            sRequestCode     = (int) (Math.random() * Math
            .pow(2, 15) + 1);
    @Rule
    public         ActivityTestRule<TestActivity> activityTestRule =
            new ActivityTestRule<>(TestActivity.class, true, true);
    private UiDevice mDevice;

    @Before
    public void setUp() throws Exception {
        sRequestCode = (int) (Math.random() * Math.pow(2, 15) + 1);
        // Resets all permissions
        PermissionsTestUtil.revokeAllPermissions();
        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(getInstrumentation());
    }

    @Test
    public void revokeAllPermissions() throws Exception {
        // Make sure permissions aren't already granted
        assertNotEquals(PackageManager.PERMISSION_GRANTED, ActivityCompat.checkSelfPermission(
                activityTestRule.getActivity(), Manifest.permission.CAMERA));
        assertNotEquals(PackageManager.PERMISSION_GRANTED, ActivityCompat.checkSelfPermission(
                activityTestRule.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION));
        // Ask for permission
        ActivityCompat.requestPermissions(activityTestRule.getActivity(),
                                          new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION},
                                          sRequestCode);
        UiObject2 allowButton = mDevice.wait(PermissionsTestUtil.ALLOW_SEARCH_CONDITION, 100);
        MatcherAssert.assertThat(allowButton.isEnabled(), Is.is(true));
        allowButton.click();
        // Wait for the click to register
        mDevice.wait(Until.hasObject(By.pkg(ApplicationTestUtil.APPLICATION_PACKAGE_NAME).depth(0)),
                     100);
        // Assert that permissions were granted
        assertEquals(PackageManager.PERMISSION_GRANTED, ActivityCompat.checkSelfPermission(
                activityTestRule.getActivity(), Manifest.permission.CAMERA));
        assertEquals(PackageManager.PERMISSION_GRANTED, ActivityCompat.checkSelfPermission(
                activityTestRule.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION));
        // Revoke all permissions.
        PermissionsTestUtil.revokeAllPermissions();
        // Make sure permissions are revoked
        assertNotEquals(PackageManager.PERMISSION_GRANTED, ActivityCompat.checkSelfPermission(
                activityTestRule.getActivity(), Manifest.permission.CAMERA));
        assertNotEquals(PackageManager.PERMISSION_GRANTED, ActivityCompat.checkSelfPermission(
                activityTestRule.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION));
    }

    @Test
    public void revokePermission() throws Exception {
        // Make sure permission isn't already granted
        assertNotEquals(PackageManager.PERMISSION_GRANTED, ActivityCompat.checkSelfPermission(
                activityTestRule.getActivity(), Manifest.permission.CAMERA));
        // Ask for permission
        ActivityCompat.requestPermissions(activityTestRule.getActivity(),
                                          new String[]{Manifest.permission.CAMERA},
                                          sRequestCode);
        UiObject2 allowButton = mDevice.wait(PermissionsTestUtil.ALLOW_SEARCH_CONDITION, 100);
        MatcherAssert.assertThat(allowButton.isEnabled(), Is.is(true));
        allowButton.click();
        // Wait for the click to register
        mDevice.wait(Until.hasObject(By.pkg(ApplicationTestUtil.APPLICATION_PACKAGE_NAME).depth(0)),
                     100);
        // Assert that permissions were granted
        assertEquals(PackageManager.PERMISSION_GRANTED, ActivityCompat.checkSelfPermission(
                activityTestRule.getActivity(), Manifest.permission.CAMERA));
        // Revoke all permissions.
        PermissionsTestUtil.revokePermission(Permission.CAMERA);
        // Make sure permissions are revoked
        assertNotEquals(PackageManager.PERMISSION_GRANTED, ActivityCompat.checkSelfPermission(
                activityTestRule.getActivity(), Manifest.permission.CAMERA));
    }

    @Test
    public void grantPermission() throws Exception {
        // Make sure permission isn't already granted
        assertNotEquals(PackageManager.PERMISSION_GRANTED, ActivityCompat.checkSelfPermission(
                activityTestRule.getActivity(), Manifest.permission.CAMERA));
        // Grants permission
        PermissionsTestUtil.grantPermission(Permission.CAMERA);
        // Assert that permissions were granted
        assertEquals(PackageManager.PERMISSION_GRANTED, ActivityCompat.checkSelfPermission(
                activityTestRule.getActivity(), Manifest.permission.CAMERA));
    }

}