package com.truethat.android.application;

import com.truethat.android.application.permissions.DefaultPermissionsModule;
import com.truethat.android.application.permissions.PermissionsModule;
import com.truethat.android.application.storage.internal.DefaultInternalStorage;
import com.truethat.android.application.storage.internal.InternalStorage;
import com.truethat.android.identity.AuthModule;
import com.truethat.android.identity.DefaultAuthModule;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */

public class App {
    private static PermissionsModule sPermissionsModule = new DefaultPermissionsModule();
    private static InternalStorage   sInternalStorage   = new DefaultInternalStorage();
    private static AuthModule        sAuthModule        = new DefaultAuthModule();

    public static PermissionsModule getPermissionsModule() {
        return sPermissionsModule;
    }

    public static void setPermissionsModule(PermissionsModule permissionsModule) {
        sPermissionsModule = permissionsModule;
    }

    public static InternalStorage getInternalStorage() {
        return sInternalStorage;
    }

    public static void setInternalStorage(
            InternalStorage internalStorage) {
        sInternalStorage = internalStorage;
    }

    public static AuthModule getAuthModule() {
        return sAuthModule;
    }

    public static void setAuthModule(AuthModule authModule) {
        sAuthModule = authModule;
    }
}
