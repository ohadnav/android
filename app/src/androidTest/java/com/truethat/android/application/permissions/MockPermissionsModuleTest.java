package com.truethat.android.application.permissions;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 25/05/2017 for TrueThat.
 */
@SuppressWarnings("ConstantConditions")
public class MockPermissionsModuleTest {
    private MockPermissionsModule mPermissionsModule;

    @Before
    public void setUp() throws Exception {
        mPermissionsModule = new MockPermissionsModule();
        assertFalse(mPermissionsModule.isPermissionGranted(null, Permission.CAMERA));
        assertFalse(mPermissionsModule.isPermissionGranted(null, Permission.READ_CONTACTS));
    }

    @Test
    public void constructor() throws Exception {
        mPermissionsModule = new MockPermissionsModule(Permission.CAMERA, Permission.READ_CONTACTS);
        assertTrue(mPermissionsModule.isPermissionGranted(null, Permission.CAMERA));
        assertTrue(mPermissionsModule.isPermissionGranted(null, Permission.READ_CONTACTS));
    }

    @Test
    public void isPermissionGranted() throws Exception {
        mPermissionsModule.grant(Permission.CAMERA);
        assertTrue(mPermissionsModule.isPermissionGranted(null, Permission.CAMERA));
    }

    @Test
    public void shouldShowRationale_permissionNotGranted() throws Exception {
        assertFalse(mPermissionsModule.shouldShowRationale(null, Permission.CAMERA));
        mPermissionsModule.setExplicitRationaleBehaviour(Permission.CAMERA, true);
        assertTrue(mPermissionsModule.shouldShowRationale(null, Permission.CAMERA));
    }

    @Test
    public void shouldShowRationale_permissionAlreadyGranted() throws Exception {
        assertFalse(mPermissionsModule.shouldShowRationale(null, Permission.CAMERA));
        mPermissionsModule.setExplicitRationaleBehaviour(Permission.CAMERA, true);
        mPermissionsModule.grant(Permission.CAMERA);
        assertFalse(mPermissionsModule.shouldShowRationale(null, Permission.CAMERA));
    }

    @Test
    public void requestIfNeeded_newPermission() throws Exception {
        mPermissionsModule.requestIfNeeded(null, Permission.CAMERA);
        assertTrue(mPermissionsModule.isPermissionGranted(null, Permission.CAMERA));
    }

    @Test
    public void requestIfNeeded_alreadyRevoked() throws Exception {
        mPermissionsModule.revokeAndForbid(Permission.CAMERA);
        mPermissionsModule.requestIfNeeded(null, Permission.CAMERA);
        assertFalse(mPermissionsModule.isPermissionGranted(null, Permission.CAMERA));
    }

    @Test
    public void revoke() throws Exception {
        mPermissionsModule.requestIfNeeded(null, Permission.CAMERA);
        assertTrue(mPermissionsModule.isPermissionGranted(null, Permission.CAMERA));
        mPermissionsModule.revokeAndForbid(Permission.CAMERA);
        assertFalse(mPermissionsModule.isPermissionGranted(null, Permission.CAMERA));
    }
}