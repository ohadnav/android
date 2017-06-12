package com.truethat.android.application.permissions;

import android.os.ParcelFileDescriptor;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.SearchCondition;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import com.truethat.android.application.ApplicationTestUtil;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */

@SuppressWarnings("WeakerAccess") public class PermissionsTestUtil {
  static final BySelector ALLOW_SELECTOR =
      By.res(ApplicationTestUtil.INSTALLER_PACKAGE_NAME, "permission_allow_button");
  static final SearchCondition<UiObject2> ALLOW_SEARCH_CONDITION = Until.findObject(ALLOW_SELECTOR);
  static final SearchCondition<UiObject2> DENY_SEARCH_CONDITION = Until.findObject(
      By.res(ApplicationTestUtil.INSTALLER_PACKAGE_NAME, "permission_deny_button"));

  public static void revokeAllPermissions() throws Exception {
    ParcelFileDescriptor res =
        getInstrumentation().getUiAutomation().executeShellCommand("pm reset-permissions");
    res.close();
    Thread.sleep(100);
  }

  public static void revokePermission(Permission permission) throws Exception {
    ParcelFileDescriptor res = getInstrumentation().getUiAutomation()
        .executeShellCommand("pm revokeAndForbid "
            + ApplicationTestUtil.APPLICATION_PACKAGE_NAME
            + " "
            + permission.getManifest());
    res.close();
    Thread.sleep(100);
  }

  public static void grantPermission(Permission permission) throws Exception {
    ParcelFileDescriptor res = getInstrumentation().getUiAutomation()
        .executeShellCommand("pm grant "
            + ApplicationTestUtil.APPLICATION_PACKAGE_NAME
            + " "
            + permission.getManifest());
    res.close();
    Thread.sleep(100);
  }
}
