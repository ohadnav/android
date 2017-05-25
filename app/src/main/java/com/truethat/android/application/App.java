package com.truethat.android.application;

import com.truethat.android.application.permissions.DefaultPermissionsModule;
import com.truethat.android.application.permissions.PermissionsModule;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */

public class App {
    private static PermissionsModule sPermissionsModule = new DefaultPermissionsModule();

    public static PermissionsModule getPermissionsModule() {
        return sPermissionsModule;
    }

    public static void setPermissionsModule(PermissionsModule permissionsModule) {
        sPermissionsModule = permissionsModule;
    }
}
