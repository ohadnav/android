package com.truethat.android.application.permissions;

import android.os.ParcelFileDescriptor;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.SearchCondition;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.support.v4.app.ActivityCompat;
import com.truethat.android.application.ApplicationTestUtil;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static com.truethat.android.application.ApplicationTestUtil.getCurrentActivity;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */

@SuppressWarnings("WeakerAccess") public class PermissionsTestUtil {
  static final BySelector ALLOW_SELECTOR =
      By.res(ApplicationTestUtil.INSTALLER_PACKAGE_NAME, "permission_allow_button");
  static final SearchCondition<UiObject2> ALLOW_SEARCH_CONDITION = Until.findObject(ALLOW_SELECTOR);
  static final SearchCondition<UiObject2> DENY_SEARCH_CONDITION = Until.findObject(
      By.res(ApplicationTestUtil.INSTALLER_PACKAGE_NAME, "permission_deny_button"));
  private static final int SLEEP_TIME = 1000;

  public static void revokeAllPermissions() throws Exception {
    ParcelFileDescriptor res =
        getInstrumentation().getUiAutomation().executeShellCommand("pm reset-permissions");
    res.close();
    Thread.sleep(SLEEP_TIME);
  }

  public static void revokePermission(Permission permission) throws Exception {
    ParcelFileDescriptor res = getInstrumentation().getUiAutomation()
        .executeShellCommand("pm revoke "
            + ApplicationTestUtil.APPLICATION_PACKAGE_NAME
            + " "
            + permission.getManifest());
    res.close();
    Thread.sleep(SLEEP_TIME);
  }

  public static void grantPermission(Permission permission) throws Exception {
    ParcelFileDescriptor res = getInstrumentation().getUiAutomation()
        .executeShellCommand("pm grant "
            + ApplicationTestUtil.APPLICATION_PACKAGE_NAME
            + " "
            + permission.getManifest());
    res.close();
    // Attempts to hit the allow button.
    ActivityCompat.requestPermissions(getCurrentActivity(),
        new String[] { permission.getManifest() }, permission.getRequestCode());
    UiObject2 allowButton = UiDevice.getInstance(getInstrumentation())
        .wait(PermissionsTestUtil.ALLOW_SEARCH_CONDITION, 100);
    MatcherAssert.assertThat(allowButton.isEnabled(), Is.is(true));
    allowButton.click();

    Thread.sleep(SLEEP_TIME);
  }
}
