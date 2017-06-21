package com.truethat.android.common.util;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;

/**
 * Proudly created by ohad on 15/06/2017 for TrueThat.
 */

public class UiUtil {
  /**
   * Hides the keyboard to free up more screen space.
   *
   * @param activity in which to hide the keyboard.
   */
  public static void hideSoftKeyboard(Activity activity) {
    InputMethodManager inputMethodManager =
        (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
    if (activity.getCurrentFocus() != null) {
      inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
  }
}
