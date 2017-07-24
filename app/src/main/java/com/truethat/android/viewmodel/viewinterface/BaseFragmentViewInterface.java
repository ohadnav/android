package com.truethat.android.viewmodel.viewinterface;

/**
 * Proudly created by ohad on 24/07/2017 for TrueThat.
 */

public interface BaseFragmentViewInterface extends BaseViewInterface {
  /**
   * @return whether the encapsulating fragment is visible to the user and resumed.
   */
  boolean isReallyVisible();
}
