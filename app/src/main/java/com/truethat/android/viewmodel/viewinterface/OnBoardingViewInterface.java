package com.truethat.android.viewmodel.viewinterface;

/**
 * Proudly created by ohad on 31/07/2017 for TrueThat.
 */

public interface OnBoardingViewInterface extends BaseViewInterface {
  /**
   * Focuses on the name edit field.
   */
  void requestNameEditFocus();

  /**
   * Hides soft keyboard to reveal more of the display.
   */
  void hideSoftKeyboard();

  /**
   * Shows soft keyboard.
   */
  void showSoftKeyboard();

  /**
   * Finishes the on boarding flow.
   */
  void finishOnBoarding();
}
