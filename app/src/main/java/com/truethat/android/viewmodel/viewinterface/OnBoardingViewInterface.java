package com.truethat.android.viewmodel.viewinterface;

import com.truethat.android.application.auth.AuthListener;

/**
 * Proudly created by ohad on 31/07/2017 for TrueThat.
 */

public interface OnBoardingViewInterface extends BaseViewInterface {
  /**
   * Focuses on the name edit field.
   */
  void requestNameEditFocus();

  /**
   * Removes the focus from the name field.
   */
  void clearNameEditFocus();

  /**
   * Hides soft keyboard to reveal more of the display.
   */
  void hideSoftKeyboard();

  /**
   * Shows soft keyboard.
   */
  void showSoftKeyboard();

  /**
   * @return the auth listener of the activity associated with this view model.
   */
  AuthListener getAuthListener();
}
