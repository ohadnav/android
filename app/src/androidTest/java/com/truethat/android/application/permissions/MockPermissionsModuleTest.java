package com.truethat.android.application.permissions;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 25/05/2017 for TrueThat.
 */
@SuppressWarnings("ConstantConditions") public class MockPermissionsModuleTest {
  private static final Permission PERMISSION = Permission.CAMERA;
  private MockPermissionsModule mPermissionsModule;

  @Before public void setUp() throws Exception {
    // Resets permission module.
    mPermissionsModule = new MockPermissionsModule();
    // Assert no permissions are granted.
    for (Permission permission : Permission.values()) {
      assertFalse(mPermissionsModule.isPermissionGranted(null, permission));
    }
  }

  @Test public void constructor() throws Exception {
    mPermissionsModule = new MockPermissionsModule(PERMISSION, Permission.PHONE);
    assertTrue(mPermissionsModule.isPermissionGranted(null, PERMISSION));
    assertTrue(mPermissionsModule.isPermissionGranted(null, Permission.PHONE));
  }

  @Test public void isPermissionGranted() throws Exception {
    // Grants permission
    mPermissionsModule.grant(PERMISSION);
    // Permission should be granted.
    assertTrue(mPermissionsModule.isPermissionGranted(null, PERMISSION));
  }

  @Test public void shouldShowRationale_permissionNotGranted() throws Exception {
    // Rationale should
    assertFalse(mPermissionsModule.shouldShowRationale(null, PERMISSION));
    mPermissionsModule.setExplicitRationaleBehaviour(PERMISSION, true);
    // Permission should be granted.
    assertTrue(mPermissionsModule.shouldShowRationale(null, PERMISSION));
  }

  @Test public void shouldShowRationale_permissionAlreadyGranted() throws Exception {
    assertFalse(mPermissionsModule.shouldShowRationale(null, PERMISSION));
    mPermissionsModule.setExplicitRationaleBehaviour(PERMISSION, true);
    // Grants permission
    mPermissionsModule.grant(PERMISSION);
    // Don't show rationale for granted permissions.
    assertFalse(mPermissionsModule.shouldShowRationale(null, PERMISSION));
  }

  @Test public void requestIfNeeded_newPermission() throws Exception {
    // requestIfNeeded grants permissions by default
    mPermissionsModule.requestIfNeeded(null, PERMISSION);
    assertTrue(mPermissionsModule.isPermissionGranted(null, PERMISSION));
  }

  @Test public void requestIfNeeded_alreadyForbidden() throws Exception {
    mPermissionsModule.forbid(PERMISSION);
    // requestIfNeeded does not override forbid.
    mPermissionsModule.requestIfNeeded(null, PERMISSION);
    assertFalse(mPermissionsModule.isPermissionGranted(null, PERMISSION));
  }

  @Test public void forbid() throws Exception {
    mPermissionsModule.forbid(PERMISSION);
    // Permission is not granted
    assertFalse(mPermissionsModule.isPermissionGranted(null, PERMISSION));
    // requested permission and still not granted.
    mPermissionsModule.requestIfNeeded(null, PERMISSION);
    assertFalse(mPermissionsModule.isPermissionGranted(null, PERMISSION));
  }

  @Test public void reset() throws Exception {
    // Forbid permission
    mPermissionsModule.forbid(PERMISSION);
    assertFalse(mPermissionsModule.isPermissionGranted(null, PERMISSION));
    // Even after request the permission is not granted.
    mPermissionsModule.requestIfNeeded(null, PERMISSION);
    assertFalse(mPermissionsModule.isPermissionGranted(null, PERMISSION));
    // Reset its state.
    mPermissionsModule.reset(PERMISSION);
    mPermissionsModule.requestIfNeeded(null, PERMISSION);
    // Should have been granted by now.
    assertTrue(mPermissionsModule.isPermissionGranted(null, PERMISSION));
  }
}