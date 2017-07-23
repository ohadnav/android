package com.truethat.android.viewmodel;

import com.truethat.android.view.fragment.BaseFragment;
import com.truethat.android.viewmodel.viewinterface.BaseFragmentViewInterface;

/**
 * Proudly created by ohad on 23/07/2017 for TrueThat.
 */

public class BaseFragmentViewModel<ViewInterface extends BaseFragmentViewInterface>
    extends BaseViewModel<ViewInterface> {
  /**
   * Triggered by {@link #getView()} on {@link BaseFragment#onVisible()}, when the fragment becomes
   * visible by the user.
   */
  public void onVisible() {
  }

  /**
   * Triggered by {@link #getView()} on {@link BaseFragment#onHidden()} ()}, when the fragment turns
   * hidden to the user.
   */
  public void onHidden() {
  }
}
