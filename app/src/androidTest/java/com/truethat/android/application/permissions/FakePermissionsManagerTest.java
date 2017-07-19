package com.truethat.android.application.permissions;

import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.ui.activity.AskForPermissionActivity;
import com.truethat.android.ui.activity.TestActivity;
import org.junit.Test;

import static com.truethat.android.application.ApplicationTestUtil.waitForActivity;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 25/05/2017 for TrueThat.
 */
@SuppressWarnings("ConstantConditions") public class FakePermissionsManagerTest
    extends BaseApplicationTestSuite {
  private static final Permission PERMISSION = Permission.CAMERA;

  @Test public void constructor() throws Exception {
    mFakePermissionsManager = new FakePermissionsManager(PERMISSION, Permission.PHONE);
    assertTrue(mFakePermissionsManager.isPermissionGranted(PERMISSION));
    assertTrue(mFakePermissionsManager.isPermissionGranted(Permission.PHONE));
  }

  @Test public void isPermissionGranted() throws Exception {
    // Grants permission
    mFakePermissionsManager.grant(PERMISSION);
    // Permission should be granted.
    assertTrue(mFakePermissionsManager.isPermissionGranted(PERMISSION));
  }

  @Test public void requestIfNeeded_newPermission() throws Exception {
    // requestIfNeeded grants permissions by default
    mFakePermissionsManager.requestIfNeeded(null, PERMISSION);
    assertTrue(mFakePermissionsManager.isPermissionGranted(PERMISSION));
  }

  @Test public void requestIfNeeded_alreadyForbidden() throws Exception {
    mFakePermissionsManager.forbid(PERMISSION);
    // requestIfNeeded does not override forbid.
    mFakePermissionsManager.requestIfNeeded(null, PERMISSION);
    assertFalse(mFakePermissionsManager.isPermissionGranted(PERMISSION));
  }

  @Test public void requestCallback_invokedWhenForbidden() throws Exception {
    // Enables invocation of request callback.
    mFakePermissionsManager.forbid(PERMISSION);
    mFakePermissionsManager.requestIfNeeded(mActivityTestRule.getActivity(), PERMISSION);
    // Should navigate to AskForPermissionActivity
    waitForActivity(AskForPermissionActivity.class);
  }

  @Test public void requestCallback_notInvokedIfAlreadyGranted() throws Exception {
    // Enables invocation of request callback.
    mFakePermissionsManager.setInvokeRequestCallback(true);
    // Grants permission
    mFakePermissionsManager.grant(PERMISSION);
    mFakePermissionsManager.requestIfNeeded(mActivityTestRule.getActivity(), PERMISSION);
    // Should stay in TestActivity
    waitForActivity(TestActivity.class);
  }

  @Test public void forbid() throws Exception {
    mFakePermissionsManager.forbid(PERMISSION);
    // Permission is not granted
    assertFalse(mFakePermissionsManager.isPermissionGranted(PERMISSION));
    // requested permission and still not granted.
    mFakePermissionsManager.requestIfNeeded(null, PERMISSION);
    assertFalse(mFakePermissionsManager.isPermissionGranted(PERMISSION));
  }

  @Test public void reset() throws Exception {
    // Forbid permission
    mFakePermissionsManager.forbid(PERMISSION);
    assertFalse(mFakePermissionsManager.isPermissionGranted(PERMISSION));
    // Even after request the permission is not granted.
    mFakePermissionsManager.requestIfNeeded(null, PERMISSION);
    assertFalse(mFakePermissionsManager.isPermissionGranted(PERMISSION));
    // Reset its state.
    mFakePermissionsManager.reset(PERMISSION);
    mFakePermissionsManager.requestIfNeeded(null, PERMISSION);
    // Should have been granted by now.
    assertTrue(mFakePermissionsManager.isPermissionGranted(PERMISSION));
  }
}