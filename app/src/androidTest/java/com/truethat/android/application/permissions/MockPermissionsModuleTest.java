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
@SuppressWarnings("ConstantConditions") public class MockPermissionsModuleTest
    extends BaseApplicationTestSuite {
  private static final Permission PERMISSION = Permission.CAMERA;

  @Test public void constructor() throws Exception {
    mMockPermissionsModule = new MockPermissionsModule(PERMISSION, Permission.PHONE);
    assertTrue(mMockPermissionsModule.isPermissionGranted(null, PERMISSION));
    assertTrue(mMockPermissionsModule.isPermissionGranted(null, Permission.PHONE));
  }

  @Test public void isPermissionGranted() throws Exception {
    // Grants permission
    mMockPermissionsModule.grant(PERMISSION);
    // Permission should be granted.
    assertTrue(mMockPermissionsModule.isPermissionGranted(null, PERMISSION));
  }

  @Test public void shouldShowRationale_permissionNotGranted() throws Exception {
    // Rationale should
    assertFalse(mMockPermissionsModule.shouldShowRationale(null, PERMISSION));
    mMockPermissionsModule.setExplicitRationaleBehaviour(PERMISSION, true);
    // Permission should be granted.
    assertTrue(mMockPermissionsModule.shouldShowRationale(null, PERMISSION));
  }

  @Test public void shouldShowRationale_permissionAlreadyGranted() throws Exception {
    assertFalse(mMockPermissionsModule.shouldShowRationale(null, PERMISSION));
    mMockPermissionsModule.setExplicitRationaleBehaviour(PERMISSION, true);
    // Grants permission
    mMockPermissionsModule.grant(PERMISSION);
    // Don't show rationale for granted permissions.
    assertFalse(mMockPermissionsModule.shouldShowRationale(null, PERMISSION));
  }

  @Test public void requestIfNeeded_newPermission() throws Exception {
    // requestIfNeeded grants permissions by default
    mMockPermissionsModule.requestIfNeeded(null, PERMISSION);
    assertTrue(mMockPermissionsModule.isPermissionGranted(null, PERMISSION));
  }

  @Test public void requestIfNeeded_alreadyForbidden() throws Exception {
    mMockPermissionsModule.forbid(PERMISSION);
    // requestIfNeeded does not override forbid.
    mMockPermissionsModule.requestIfNeeded(null, PERMISSION);
    assertFalse(mMockPermissionsModule.isPermissionGranted(null, PERMISSION));
  }

  @Test public void requestCallback_invokedWhenForbidden() throws Exception {
    // Enables invocation of request callback.
    mMockPermissionsModule.forbid(PERMISSION);
    mMockPermissionsModule.requestIfNeeded(mActivityTestRule.getActivity(), PERMISSION);
    // Should navigate to AskForPermissionActivity
    waitForActivity(AskForPermissionActivity.class);
  }

  @Test public void requestCallback_notInvokedIfAlreadyGranted() throws Exception {
    // Enables invocation of request callback.
    mMockPermissionsModule.setInvokeRequestCallback(true);
    // Grants permission
    mMockPermissionsModule.grant(PERMISSION);
    mMockPermissionsModule.requestIfNeeded(mActivityTestRule.getActivity(), PERMISSION);
    // Should stay in TestActivity
    waitForActivity(TestActivity.class);
  }

  @Test public void forbid() throws Exception {
    mMockPermissionsModule.forbid(PERMISSION);
    // Permission is not granted
    assertFalse(mMockPermissionsModule.isPermissionGranted(null, PERMISSION));
    // requested permission and still not granted.
    mMockPermissionsModule.requestIfNeeded(null, PERMISSION);
    assertFalse(mMockPermissionsModule.isPermissionGranted(null, PERMISSION));
  }

  @Test public void reset() throws Exception {
    // Forbid permission
    mMockPermissionsModule.forbid(PERMISSION);
    assertFalse(mMockPermissionsModule.isPermissionGranted(null, PERMISSION));
    // Even after request the permission is not granted.
    mMockPermissionsModule.requestIfNeeded(null, PERMISSION);
    assertFalse(mMockPermissionsModule.isPermissionGranted(null, PERMISSION));
    // Reset its state.
    mMockPermissionsModule.reset(PERMISSION);
    mMockPermissionsModule.requestIfNeeded(null, PERMISSION);
    // Should have been granted by now.
    assertTrue(mMockPermissionsModule.isPermissionGranted(null, PERMISSION));
  }
}