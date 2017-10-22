package com.truethat.android.viewmodel.viewinterface;

import android.widget.Toast;
import com.truethat.android.view.activity.BaseActivity;
import eu.inloop.viewmodel.IView;

/**
 * Proudly created by ohad on 20/07/2017 for TrueThat.
 */

public interface BaseViewInterface extends IView {
  /**
   * Displays a {@link Toast}.
   *
   * @param text to show within the toast.
   */
  void toast(String text);

  /**
   * @return the activity associated with this view model.
   */
  BaseActivity getBaseActivity();

  /**
   * @return the view TAG to be used for logging.
   */
  String getTAG();
}
